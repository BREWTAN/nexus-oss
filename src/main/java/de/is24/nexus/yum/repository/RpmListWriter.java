package de.is24.nexus.yum.repository;

import static de.is24.nexus.yum.repository.YumMetadataGenerationTask.isActive;
import static java.io.File.separator;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.io.IOUtils.writeLines;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RpmListWriter {
  private static final int POSITION_AFTER_SLASH = 1;
  private static final Logger LOG = LoggerFactory.getLogger(RpmListWriter.class);

  private final YumGeneratorConfiguration config;
  private final File rpmListFile;
  private final ListFileFactory fileFactory;

  public RpmListWriter(YumGeneratorConfiguration config, ListFileFactory fileFactory) {
    this.config = config;
    this.fileFactory = fileFactory;
    this.rpmListFile = fileFactory.getRpmListFile(config.getId());
  }

  public File writeList() throws IOException {
    if (rpmListFile.exists()) {
      LOG.info("Reuse existing rpm list file : {}", rpmListFile);
      List<String> rpmFileList = pruneToExistingRpms();

      if (isNotBlank(config.getVersion())) {
        return extractVersionOfListFile(rpmFileList);
      }

      if (isNotBlank(config.getAddedFile())) {
        addNewlyAddedRpmFileToList(rpmFileList);
      }

      writeRpmFileList(rpmFileList);
    } else {
      rewriteList();
    }

    return rpmListFile;
  }

  private File extractVersionOfListFile(List<String> files) throws IOException {
    List<String> filesWithRequiredVersion = new ArrayList<String>();
    for (String file : files) {
      if (hasRequiredVersion(file)) {
        filesWithRequiredVersion.add(file);
      }
    }

    File rpmVersionizedListFile = fileFactory.getRpmListFile(config.getId(), config.getVersion());
    writeRpmFileList(filesWithRequiredVersion, rpmVersionizedListFile);
    return rpmVersionizedListFile;
  }

  private boolean hasRequiredVersion(String file) {
    String[] segments = file.split("\\/");
    return (segments.length >= 2) && config.getVersion().equals(segments[segments.length - 2]);
  }

  private void addNewlyAddedRpmFileToList(List<String> files) throws IOException {
    final int startPosition = config.getAddedFile().startsWith("/") ? POSITION_AFTER_SLASH : 0;
    final String filename = config.getAddedFile().substring(startPosition);

    if (!files.contains(filename)) {
      files.add(filename);
      LOG.info("Added rpm {} to file list.", filename);
    } else {
      LOG.info("Rpm {} already exists in fie list.", filename);
    }
  }

  private List<String> pruneToExistingRpms() throws IOException {
    List<String> files = readRpmFileList();
    for (int i = 0; i < files.size(); i++) {
      if (!new File(config.getBaseRpmDir(), files.get(i)).exists()) {
        LOG.info("Removed {} from rpm list.", files.get(i));
        files.remove(i);
        i--;

      }
    }
    return files;
  }

  private void writeRpmFileList(Collection<String> files) throws IOException {
    writeRpmFileList(files, rpmListFile);
  }

  private void writeRpmFileList(Collection<String> files, File rpmListOutputFile) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(rpmListOutputFile);
    try {
      writeLines(files, "\n", outputStream);
      if (files.isEmpty()) {
        LOG.info(
            "Write non existing package to rpm list file {} to avoid an empty packge list that would cause createrepo to scan the whole directory",
            rpmListOutputFile);
        write(".foo/.bar.rpm/to-avoid-an-empty-rpm-list-file/that-would-cause-createrepo-to-scan-the-whole-repo.rpm", outputStream);
      }
    } finally {
      outputStream.close();
    }
    LOG.info("Wrote {} rpm packages to rpm list file {} .", files.size(), rpmListFile);
  }

  @SuppressWarnings("unchecked")
  private List<String> readRpmFileList() throws IOException {
    FileInputStream inputStream = new FileInputStream(rpmListFile);
    try {
      return readLines(inputStream);
    } finally {
      inputStream.close();
    }
  }

  private void rewriteList() throws IOException {
    if (config.isSingleRpmPerDirectory()) {
      rewriteFileList(getSortedFilteredFileList());
    } else {
      writeRpmFileList(getRelativeFilenames(getRpmFileList()), rpmListFile);
    }
  }

  private List<String> getRelativeFilenames(Collection<File> rpmFileList) {
    String absoluteBasePath = config.getBaseRpmDir().getAbsolutePath() + separator;

    List<String> result = new ArrayList<String>(rpmFileList.size());
    for (File rpmFile : rpmFileList) {
      result.add(getRelativePath(rpmFile, absoluteBasePath));
    }
    return result;
  }

  private void rewriteFileList(Map<String, String> fileMap) {
    try {
      Writer writer = new FileWriter(rpmListFile);
      try {
        for (Entry<String, String> entry : fileMap.entrySet()) {
					if (!isActive()) {
            return;
          }
          writer.append(format("%s%s\n", entry.getKey(), entry.getValue()));
        }
      } finally {
        writer.close();
      }
      LOG.info("Wrote temporary package list to {}", rpmListFile.getAbsoluteFile());
    } catch (IOException e) {
      LOG.warn("Could not write temporary package list file", e);
    }

  }

  private Map<String, String> getSortedFilteredFileList() {
    String absoluteBasePath = config.getBaseRpmDir().getAbsolutePath() + separator;

    Map<String, String> fileMap = new TreeMap<String, String>();

    for (File file : getRpmFileList()) {
      File parentFile = file.getParentFile();
      if (matchesRequestedVersion(parentFile)) {
        String parentDir = getRelativePath(parentFile, absoluteBasePath);
        putLatestArtifactInMap(parentDir, file.getName(), fileMap);
      }
    }
    return fileMap;
  }

  private void putLatestArtifactInMap(String parentDir, String filename, Map<String, String> fileMap) {
    if (!fileMap.containsKey(parentDir) || (filename.compareTo(fileMap.get(parentDir)) > 0)) {
      fileMap.put(parentDir, filename);
    }
  }

  @SuppressWarnings("unchecked")
  private Collection<File> getRpmFileList() {
    Collection<File> result = listFiles(config.getBaseRpmDir(), new String[] { "rpm" }, true);
    return result;
  }

  private String getRelativePath(File file, String baseDirectory) {
    String filePath = file.getAbsolutePath() + (file.isDirectory() ? separator : "");
    if (filePath.startsWith(baseDirectory)) {
      filePath = filePath.substring(baseDirectory.length());
    }
    return filePath;
  }

  private boolean matchesRequestedVersion(File parentFile) {
    return (config.getVersion() == null) || parentFile.getName().equals(config.getVersion());
  }

}
