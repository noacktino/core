# Core
This folder contains the core Java and Maven-based implementation of OpenInfRA. The project page can be found [here](http://www.b-tu.de/openinfra/).

LOC (version 1.4.2): approx 25.000

# Installation

To run OpenInfRA different prerequisites must be complied. At this point we will give a broad overview about the necessary components and their configuration.
OpenInfRA consists of the following parts:

## Application
The application is written in Java and must be compiled with Java 7. It must be packed into a _war_ file and run on a server. Currently, the application is optimized for Apache Tomcat. There are only a few handles necessary to configure the application. The main [configuration file](openinfra_core/src/main/resources/de/btu/openinfra/backend/properties/OpenInfRA.properties) must be adapted to the current needs. The different configuration parameters are commented and need no further explanations. The prime configurations are the database connection and file path properties. These must be set correctly in order to run the application.

The OpenInfRA application is based on the following software stack:
- JAX-RS: [Jersey](https://jersey.java.net/)
- Security: [Apache Shiro](http://shiro.apache.org/)
- JPA: [EclipseLink](http://www.eclipse.org/eclipselink/)
- Database: [PostgreSQL](http://www.postgresql.org/) + [PostGis](http://postgis.net/)

![OpenInfRA Softwarestack](https://github.com/OpenInfRA/core/blob/master/img/sw-stack.JPG "OpenInfRA Softwarestack")

## Database
The [database](https://github.com/OpenInfRA/database) is necessary to provide a data storage for OpenInfRA. Further instructions can be found in the appropriated repositoriy.

## Solr
To make use of Solr in OpenInfRA two prerequisites must be complied. First the [server](https://github.com/OpenInfRA/solr_server) itself must be installed. Second the [core definition](https://github.com/OpenInfRA/solr_core) must be installed in the specified file path. Further instructions can be found in the appropriated repositories.

## WebGis/GeoServer
A detailed description is provided in the specific folder: [GXC](https://github.com/OpenInfRA/GXC).

## ImageMagick
OpenInfRA provides a file upload. This upload requires [ImageMagick](http://www.imagemagick.org) to generate and produce different image representations.

- PDF conversion requires [ghostscript](http://www.ghostscript.com/)
- Conversion of raw file formats (such as DNG) require [UFRaw](http://ufraw.sourceforge.net/) under Linux-based systems.

# Some Internals
This section shows some starting points and describes a few details. _Project_ and _TopicCharacteristic_ are used as running examples. As the name states, 'Project' refers to an OpenInfRA project. A 'TopicCharacteristic' is an abstract container which groups a set of objects by the description and consolidation of specific attributes. An object is called _TopicInstance_ and it defines attribute values consolidated by a 'TopicCharacteristic'. This leads to the following correlation: a set of TopicInstances are type of a specific TopicCharacteristic.

## Model
The following picture shows the 'Project' and the 'TopicCharacteristic' as _model_ _objects_. 'Model objects' refer to the persistence layer. Native SQL queries or JPA queries schould be placed here. This helps to keep the code nice and clean.

![OpenInfRA Model](https://github.com/OpenInfRA/core/blob/master/img/model.png "OpenInfRA Model")

## POJO
The following picture shows the 'Project' and the 'TopicCharacteristic' as _POJO_ _objects_. 'POJO objects' are data containers. These containers are used to transfer data from the application core to the REST API.

![OpenInfRA POJOI](https://github.com/OpenInfRA/core/blob/master/img/pojo.png "OpenInfRA POJO")

## DAO
Database access is utilized by means of a _DAO_ _pattern_. There exists a DAO class for each _model_ _object_. DAO classes are used to transform 'model objects' into 'POJO objects' and vice versa. This is a litle bit implementation intensive but leads to a maximum of data control. Thus, it is possible to hide data in the REST API and to enrich the REST API with additional information without invoking side effects to the persistence layer.

![OpenInfRA DAO](https://github.com/OpenInfRA/core/blob/master/img/dao.png "OpenInfRA DAO")

## Entity Manager
The _entity_ _manager_ is very important for the DAO classes and the reaction time of the application since it utilizes the database access. Each DAO class uses its own 'entity manager'. In order to provide fast access it exists an _EntityManagerFactoryCache_ which provides the administation of 'entity manager' objects for DAO classes.

![OpenInfRA EM](https://github.com/OpenInfRA/core/blob/master/img/em.png "OpenInfRA EM")

## Database Schemas
OpenInfRA provides different _database_ _schemas_. Each 'database schema' is optimized for specific needs:
- _system_: The 'system schema' contains abstract data and information which is used to derive project schemas.
- _project_: A 'project schema' contains only project specific data without meta data. There are several project schemas. Each project schema provides its own UUID.
- _meta data_: The 'meta data schema' contains additional information of a project.
- _rbac_: The 'rbac schema' contains information for the role-based access control system. This includes user information, roles and permissions.
- _webapp_: The 'webapp schema' provides additional information for GUI applications.
- _files_: The 'files schema' provides data of the file upload system.
- _search_: The 'search schema' is not a real database schema, it only provides an access point for the search engine.

![OpenInfRA Database Schemas](https://github.com/OpenInfRA/core/blob/master/img/schemas.png "OpenInfRA Database Schemas")

Adding a new schema can be done very easy by the following steps:
- 1. Create schema on the database level.
- 2. Generate necessary model objects.
- 3. Register the new schema in the _OpenInfraSchemas_ enumeration
- 4. Create POJO, DAO and RBAC classes.
- 5. Register the new schema in the _EntityManagerFactoryCache_.
- 6. Register resources and URLs in the REST API.

# TODO
- The JUnit tests have to be extended.
- Logging isn't implemented appropriately.

### Revision of REST-Resources and DAO-Classes
The initial database schema didn't consider an UUID for the identification of associations. An association object was initally identified by the UUIDs of the related objects e.g. the 'attribute type' to 'attribute type group' association was identified by the UUID of the 'attribute type' and the 'attribute type group'. Each of the aforementioned objects relates to a POJO object. Thus, it became necessary to equip each POJO with an UUID (especially the association object) in order to provide a generic API. However, the old access stratedgy is still available and should be revised.
