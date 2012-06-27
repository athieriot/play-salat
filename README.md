# MongoDB Salat plugin for Play Framework 2
Salat is a ORM for MongoDBs scala driver called Casbah.

The plugin's functionality simpifies the use of salat by presenting a simple "play style" configuration and binders for `ObjectId`

 * https://github.com/mongodb/casbah
 * https://github.com/novus/salat

[![Build Status](https://secure.travis-ci.org/leon/play-salat.png)](http://travis-ci.org/leon/play-salat)

## Installation

Use g8 to start a new salat enabled play project

### Install g8 on OSX using homebrew
    
    brew update && brew install giter8

Or read about the other ways to install [giter8 here](https://github.com/n8han/giter8)

Then run

    g8 leon/play-salat.g8

It will ask you a couple of questions, and your ready to rock 'n roll.

### Manual installation
Start by adding the plugin, in your `project/Build.scala`

    val appDependencies = Seq(
      "se.radley" %% "play-plugins-salat" % "1.0.6"
    )

Then we can add the implicit converstions to and from ObjectId by adding to the routesImport and add ObjectId to all the templates

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      routesImport += "se.radley.plugin.salat.Binders._",
      templatesImport += "org.bson.types.ObjectId"
    )

We now need to register the plugin, this is done by creating(or appending) to the `conf/play.plugins` file

    500:se.radley.plugin.salat.SalatPlugin

We continue to edit the `conf/application.conf` file. We need to disable some plugins that we don't need.
Add these lines:

    dbplugin = disabled
    evolutionplugin = disabled
    ehcacheplugin = disabled

## Configuration
now we need to setup our connections. The plugin is modeled after how plays DB plugin is built.

    mongodb.default.db = "mydb"
    # Optional values
    #mongodb.default.host = "127.0.0.1"
    #mongodb.default.port = 27017
    #mongodb.default.user = "leon"
    #mongodb.default.password = "123456"

	# MongoURI
	# ~~~~~
	# a MongoURI can also be used http://www.mongodb.org/display/DOCS/Connections
	# mongodb.default.uri = "mongodb://127.0.0.1:27017,mongodb.org:1337/salat-test"

	# WriteConcern
	# ~~~~~
	# Can be any of the following
	#
	# fsyncsafe - Exceptions are raised for network issues and server errors; Write operations wait for the server to flush data to disk.
	# replicassafe - Exceptions are raised for network issues and server errors; waits for at least 2 servers for the write operation.
	# safe - Exceptions are raised for network issues and server errors; waits on a server for the write operation.
	# normal - Exceptions are raised for network issues but not server errors.

	#mongodb.default.writeconcern = "safe"

	# Replica sets
	# ~~~~~
	# http://www.mongodb.org/display/DOCS/Why+Replica+Sets
	#
	# To user a replicaset instead of a single host, omit optional values and use the configuration below instead.
	# Since replica sets use public key authentication, user and password won't work together with the replicaset option.

	#mongodb.default.replicaset {
	#    host1.host = "10.0.0.1"
	#
	#    host2.host = "10.0.0.2"
	#    host2.port = 27018
	#}

## More that one DB?
If you would like to connect to two databases you need to create two source names

    mongodb.myotherdb.db = "otherdb"

Then you can call `mongoCollection("collectionname", "myotherdb")`

## What a model looks like
All models must be case classes otherwise salat doesn't know how to properly transform them into MongoDBObject's

    package models

    import play.api.Play.current
    import java.util.Date
    import com.novus.salat._
    import com.novus.salat.annotations._
    import com.novus.salat.dao._
    import com.mongodb.casbah.Imports._
    import se.radley.plugin.salat._
    import mongoContext._

    case class User(
      id: ObjectId = new ObjectId,
      username: String,
      password: String,
      address: Option[Address] = None,
      added: Date = new Date(),
      updated: Option[Date] = None,
      deleted: Option[Date] = None,
      @Key("company_id")company: Option[ObjectId] = None
    )

    object User extends ModelCompanion[User, ObjectId] {
      val collection = mongoCollection("users")
      val dao = new SalatDAO[User, ObjectId](collection = collection) {}

      def findOneByUsername(username: String): Option[User] = dao.findOne(MongoDBObject("username" -> username))
      def findByCountry(country: String) = dao.find(MongoDBObject("address.country" -> country))
    }

## Mongo Context
All models must contain an implicit salat Context. The context is somewhat like a hibernate dialect.
You can override mapping names and configure how salat does it's type hinting. read more about it [here](https://github.com/novus/salat/wiki/CustomContext)

In the sample there is a custom `mongoContext`, partly because we need to add plays classloader to salat so it knows when to reload it's graters,
but also so we can override all models id fields to be serialized to MongoDB's _id.

## Testing
For test purpose only, you can add the EmbedConnection trait to your tests classes

It will provide a standalone instance of a MongoDB server using this project:

 * https://github.com/michaelmosmann/embedmongo.flapdoodle.de

By default, it will open a connection (before the test suite) on the port 12345 and close it (after that)
You can change this default value by overriding embedConnectionPort() method

At last, you may want to use this FakeApplication configuration :

val FakeMongoApp = FakeApplication(additionalConfiguration = Map("mongodb.default.port" -> 12345),
                                   additionalPlugins = Seq("se.radley.plugin.salat.SalatPlugin"))

 - [Sample](https://github.com/leon/play-salat/tree/master/sample)

## Enums?
If your using Scala Enumerations have a look at my play-enumeration project.

- [play-enumeration](https://github.com/leon/play-enumeration)
