package restel.integration;

import com.techconative.restel.core.RestelApplication;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class IntegrationTests {

  private Network network = Network.newNetwork();

  private GenericContainer mongo =
      new GenericContainer(DockerImageName.parse("mongo"))
          .withNetwork(network)
          .withNetworkAliases("mongo")
          .withEnv(Map.of("MONGODB_URI", "mongodb://mongo:27017/jsonbox-io-dev"))
          .withExposedPorts(27017);

  private GenericContainer jsonBox =
      new GenericContainer(DockerImageName.parse("jsonbox_jsonbox"))
          .dependsOn(mongo)
          .withNetwork(network)
          .withExposedPorts(3000);

  @BeforeEach
  void setUp() {
    mongo.start();
    jsonBox.setPortBindings(List.of("3000", "3000"));
    jsonBox.start();
  }

  @AfterEach
  void tearDown() {
    mongo.close();
    jsonBox.close();
    network.close();
  }

  @Test
  void testUseCases() {
    System.setProperty(
        "app.excelFile", "/Users/kannanr/Desktop/projects/Restel/quickstart/jsonbox_test.xlsx");
    RestelApplication.main(null);
  }
}
