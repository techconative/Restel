package restel.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.techconative.restel.core.RestelApplication;
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
          .withExposedPorts(27017);

  private GenericContainer jsonBox =
      new GenericContainer(DockerImageName.parse("jsonbox_jsonbox"))
          .dependsOn(mongo)
          .withNetwork(network)
          .withEnv(Map.of("MONGODB_URI", "mongodb://mongo:27017/jsonbox-io-dev"))
          .withExposedPorts(3000);

  @BeforeEach
  void setUp() {
    mongo.start();
    jsonBox.start();
    Integer mappedPort = jsonBox.getMappedPort(3000);
    System.setProperty("PORT", mappedPort.toString());
  }

  @AfterEach
  void tearDown() {
    mongo.close();
    jsonBox.close();
    network.close();
  }

  @Test
  void testUseCases() {
    System.setProperty("app.excelFile", "src/test/resources/jsonbox_test.xlsx");
    RestelApplication app = new RestelApplication();
    assertTrue(app.executeTests());
  }
}
