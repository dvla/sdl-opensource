package uk.gov.dvla.sml.datastore

import com.mongodb.{MongoClient, MongoClientOptions, MongoCredential, ServerAddress}
import org.mongodb.morphia.{Key, Datastore, Morphia}
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.dvla.sml.ManagedService

import scala.collection.JavaConversions._
import scala.util.Try

object MongoDatastore {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[MongoDatastore].getName)

  def createClient(config: MongoDatastoreConfig): MongoClient = {
    val mongoClientOptions: MongoClientOptions = MongoClientOptions.builder
      .readPreference(config.mongo.getReadPreference).build

    if (config.mongo.getCredentials == null || config.mongo.getCredentials.isEmpty) {
      new MongoClient(parseServerAddresses(config.mongo.getServers.toList), mongoClientOptions)
    } else {
      new MongoClient(
        parseServerAddresses(config.mongo.getServers.toList),
        parseCredentials(config.mongo.getCredentials.toList, config.mongo.getDatabase),
        mongoClientOptions)
    }
  }

  def createDatastore(config: MongoDatastoreConfig): Datastore = {
    prepareMorphia(createClient(config), config)
  }

  private def parseServerAddresses(servers: List[String]): List[ServerAddress] = {
    servers.flatMap(parseServerAddress) match {
      case Nil => throw new IllegalArgumentException("No valid mongo server address were provided")
      case addresses => addresses
    }
  }

  private def parseServerAddress(server: String): Option[ServerAddress] = {
    val splitServer: Array[String] = server.split(":")
    if (splitServer.length == 2) {
      val port = Integer.parseInt(splitServer(1))
      Some(new ServerAddress(splitServer(0), port))
    } else if (splitServer.length == 1) {
      Some(new ServerAddress(splitServer(0)))
    } else {
      logger.warn("Ignoring invalid server address %s in config list", server)
      None
    }
  }

  private def parseCredentials(credentials: List[String], database: String): List[MongoCredential] = {
    credentials.flatMap { credential =>
      val splitCredential: Array[String] = credential.split(":")
      if (splitCredential.length == 2) {
        Some(
          MongoCredential.createMongoCRCredential(splitCredential(0), database, splitCredential(1).toCharArray)
        )
      }
      else {
        logger.warn(String.format("Ignoring invalid credential %s in config list", credential))
        None
      }
    } match {
      case Nil => throw new IllegalArgumentException("No valid mongo credentials were provided")
      case mongoCredentials => mongoCredentials
    }
  }

  private def prepareMorphia(client: MongoClient, config: MongoDatastoreConfig): Datastore = {
    logger.debug("Preparing morphia mapper for package: {}", config.domainPackage)

    val morphia: Morphia = new Morphia
    config.domainPackage.foreach(morphia.mapPackage)

    val morphiaDatastore: Datastore = morphia.createDatastore(client, config.mongo.getDatabase)
    if (config.mongo.isEnsureIndexes) morphiaDatastore.ensureIndexes(true)

    morphiaDatastore
  }

}

abstract class MongoDatastore(config: MongoDatastoreConfig) extends ManagedService {

    val mongoClient = MongoDatastore.createClient(config)
    val datastore = MongoDatastore.createDatastore(config)

    override def isAlive: Boolean = Try(mongoClient.getDatabaseNames).isSuccess

    override def start(): Unit = {
      // do nothing
    }

    override def stop(): Unit = {
      mongoClient.close()
    }

   protected def save[T](token: T): Key[T] = datastore.save(token)

}
