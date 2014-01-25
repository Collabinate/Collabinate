# Collabinate

## Introduction
Collaborative and social technologies have evolved from a consumer novelty to become a software necessity.  Mobile, enterprise, and other multi-user software systems are increasingly incorporating social sharing, microblogging, and activity feed functionality.  Until now, this has required either conforming and contorting to accomodate an existing "framework", or developing the needed features from scratch.

Collabinate aims to provide simple, modular functionality to any level needed so that you can add high performance collaboration, microblogging, and activity feed features to your existing applications with minimal effort.

### What Collabinate Does
Collabinate provides a means of tracking the relationships between entities in a software system so that a feed of information of interest to any of those entities can be retrieved and displayed quickly. This information can be displayed as a standard activity stream, or handled in any manner desired by the application.

### What Collabinate Does Not Do
Collabinate is not meant to replace the main data storage mechanism for the entities in a software system.  It is also not intended to manage the authentication or authorization aspects of your application - including the per-user security aspects of the data it handles.

## Definitions
Collabinate uses a concrete set of terminology to describe the data and operations it handles.

* **Entity**: An object in the system that can have a stream associated with it.  A person in Facebook would be an entity, as would an insurance policy in an insurance system.
* **User**: A special type of entity that represents a user of the software system. Users can comment on activities, follow (or "friend") entities, and access a feed based on these follow relationships. An example of a user would be someone with a Twitter account, or an insurance agent with a login to an insurance system.
* **Stream**: A collection of activities that contain information about an entity. The collection of comments on a YouTube video is a stream, as well as the set of claims on an insurance policy.
* **Activity**: A single dated entry within the stream of an entity. These items utilize the [ActivityStreams](http://activitystrea.ms/) standard for formatting. A comment on a Pinterest item would be an activity, and so would a premium change on an insurance policy.
* **Comment**: A response to an activity by a user. A reply to a Yammer post is a comment, as well as a question asked about coverage on an insurance policy by an agent.
* **Feed**: The collection of activities in the streams of all entities that a user follows. Cover Stories in Flipboard are a feed, as are the list of changes to all insurance policies sold by an agent.

## Server Operations
In order to keep the functionality of the server simple, the Collabinate Server supports a minimal set of operations.  It allows following of entities by users, creation of activities on entities, and commenting on stream items by users. It also allows the retrieval of streams for entities, and the retrieval of feeds for users.

## Architecture

![Collabinate Architecture](https://github.com/Collabinate/Collabinate/raw/master/documentation/CollabinateArchitecture.png)
