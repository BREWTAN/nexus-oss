package de.is24.nexus.yum.rest;

import static org.junit.Assert.assertEquals;
import java.io.File;
import org.junit.Test;
import org.restlet.data.MediaType;
import de.is24.nexus.yum.repository.YumRepository;


public class YumFileRepresentationTest {
  @Test
  public void shouldReturnXmlFile() throws Exception {
    YumFileRepresentation representation = createRepresentation("repomd.xml");
    assertEquals(MediaType.APPLICATION_XML, representation.getMediaType());
  }

  @Test
  public void shouldReturnGzFile() throws Exception {
    YumFileRepresentation representation = createRepresentation("primary.xml.gz");
    assertEquals(MediaType.APPLICATION_GNU_ZIP, representation.getMediaType());
  }

  @Test
  public void shouldReturnAllFile() throws Exception {
    YumFileRepresentation representation = createRepresentation("primary.txt");
    assertEquals(MediaType.APPLICATION_ALL, representation.getMediaType());
  }

  private YumFileRepresentation createRepresentation(String filename) {
    return new YumFileRepresentation(new UrlPathInterpretation(null, filename,
        false), new YumRepository(new File("."), null, null));
  }
}
