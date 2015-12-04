package uk.gov.dvla.service.testing.dropwizard

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.massrelevance.dropwizard.ScalaApplication
import com.sun.jersey.api.client.Client
import io.dropwizard.Configuration
import io.dropwizard.cli.ServerCommand
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.lifecycle.ServerLifecycleListener
import io.dropwizard.setup.{Bootstrap, Environment}
import net.sourceforge.argparse4j.inf.Namespace
import org.eclipse.jetty.server.{ServerConnector, Server}
import org.scalatest.{BeforeAndAfterAll, Suite}

trait DropwizardSpec[C <: Configuration] extends Suite with BeforeAndAfterAll {
  private var jettyServer: Option[Server] = None

  val configPath: String
  val application: ScalaApplication[C]
  val port: Int = 6666
  var httpClient: Client = _

  def ifJettyNotRunning(block: => Unit) {
    if (jettyServer.isEmpty) {
      block
    }
  }

  override protected def beforeAll(): Unit = ifJettyNotRunning {
    System.setProperty("dw.server.applicationConnectors[0].port", port.toString)

    val bootstrap = new Bootstrap[C](application) {
      override def run(configuration: C,environment: Environment): Unit = {
        println("Running Bootstrap")
        environment.lifecycle.addServerLifecycleListener(new ServerLifecycleListener {
          def serverStarted(server: Server) {
            println("Starting servers")
            jettyServer = Some(server)
          }
        })
        super.run(configuration, environment)
        httpClient = new JerseyClientBuilder(environment).build("testHttpClient")
      }
    }

    application.initialize(bootstrap)

    val command = new ServerCommand[C](application)
    val file = ImmutableMap.builder[String, AnyRef]

    if (!Strings.isNullOrEmpty(configPath)) {
      file.put("file", configPath)
    }
    val namespace = new Namespace(file.build)
    command.run(bootstrap, namespace)
  }

  override protected def afterAll(): Unit = {

    val props = System.getProperties.propertyNames
    while (props.hasMoreElements) {
      val keyString: String = props.nextElement.asInstanceOf[String]
      if (keyString.startsWith("dw.")) {
        System.clearProperty(keyString)
      }
    }
    jettyServer.foreach(_.stop())
  }
}
