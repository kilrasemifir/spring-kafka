# Travail Dirigé

## Prérequis
### Software
- [JDK 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot)
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (ou autre IDE)

### Connaissances
- [Spring Boot](https://spring.io/projects/spring-boot)
- Les bases de [Kafka](https://kafka.apache.org/)

## Objectifs

Le but de ce travail dirigé est de mettre en place un système de traitement de données en temps réel avec Spring et Kafka.

Pour ce faire nous allons créer 3 applications Spring Boot:
- __publisher__: une API HTTP qui permet d'envoyer des messages à Kafka
- __consumer__: une application qui consomme les messages de Kafka et les affiche dans la console
- __stream__: une application qui consomme les messages de Kafka et les transforme avant de les envoyer à un autre topic

## Exercice 1: Simple Publisher

### 1. Création des applications

Créez 3 applications Spring Boot avec [Spring Initializr](https://start.spring.io/) ou avec votre IDE.

Pour chacune des applications, ajoutez les dépendances suivantes:
- __publisher__: 
    - `spring-boot-starter-web`: pour pouvoir faire une API HTTP
    - `spring-kafka`: pour pouvoir envoyer des messages à Kafka
- __consumer__:
    - `spring-kafka`: pour pouvoir consommer des messages de Kafka
- __stream__:
    - `spring-kafka`: pour pouvoir consommer et publier des messages à Kafka
    - `kafka-streams`: pour pouvoir transformer les messages

> __Note__: Ce sont les dépendances minimales pour pouvoir faire fonctionner les applications. 
> Vous pouvez ajouter d'autres dépendances si vous en avez besoin.

Voici ce que vous devriez obtenir si vous utilisez gradle comme gestionnaire de dépendances pour l'app __publisher__:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.kafka:spring-kafka'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}
```

### 2. Configuration de Kafka

Nous allons avoir besoin d'un Kafka pour pouvoir faire fonctionner nos applications.

Pour cela, nous allons utiliser [docker-compose](https://docs.docker.com/compose/) pour lancer un Kafka dans un container Docker.

Créez un fichier `docker-compose.yml` contenan au minimum un kafka à la racine de votre projet.

Voici un exemple de fichier `docker-compose.yml`:

```yaml
version: '3.7'
services:
    zookeeper:
        image: confluentinc/cp-zookeeper:6.2.0
        hostname: zookeeper
        container_name: zookeeper
        ports:
        - "2181:2181"
        environment:
            ZOOKEEPER_CLIENT_PORT: 2181
            ZOOKEEPER_TICK_TIME: 2000
    
    kafka:
        image: confluentinc/cp-kafka:6.2.0
        hostname: kafka
        container_name: kafka
        depends_on:
        - zookeeper
        ports:
        - "9092:9092"
        environment:
            KAFKA_BROKER_ID: 1
            KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
            KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
            KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
            KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
```

### 3. Configuration des applications

Nous allons maintenant configurer nos applications pour qu'elles puissent communiquer avec Kafka.

Voici le schéma de communication entre les applications :
![schema](assets/simple-pub-sub.svg)

#### 3.1. Configuration du **publisher**

##### 3.1.1. Création du Publisher
Créer une classe `SimplePublisher`. Cette classe va permettre d'envoyer des messages à Kafka.

Pour ce faire nous avons besoin d'une instance de `KafkaTemplate<>` qui va nous permettre d'envoyer des messages à Kafka.

KafkaTemplate possède deux paramètres génériques :
- Le premier paramètre est le type de la clé du message
- Le second paramètre est le type du message

Pour cet exercice nous allons utiliser des messages de type `String` et des clés de type `String` donc nous allons 
utiliser `KafkaTemplate<String, String>`.

Injectez une instance de `KafkaTemplate<String, String>` dans votre classe `SimplePublisher`.

> __Note__: Pour injecter une instance de `KafkaTemplate` vous pouvez utiliser l'annotation `@Autowired` ou en passant par le constructeur.

```java
@Service
public class SimplePublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SimplePublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
}
```

Ajouter une méthode `sendMessage` qui permet d'envoyer un message à Kafka.

Pour publier un message à Kafka, nous utilisons la méthode `send` de `KafkaTemplate`:

```java
class SimplePublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public SimplePublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    void sendMessage(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
    }
}
```

Elle prend 2 paramètres:
- Le nom du topic
- Le message à envoyer

Il est possible d'utiliser la méthode `send` avec 3 paramètres:
- Le nom du topic
- La clé du message
- Le message à envoyer

Dans notre cas, nous ne gérons pas les clés des messages donc nous allons utiliser la méthode `send` avec 2 paramètres.

##### 3.1.2. Création de l'API HTTP
Nous allons maintenant créer une API HTTP qui permet d'envoyer des messages à Kafka.

Créer une classe `MessageController` avec l'annotation `@RestController` qui va permettre de créer une API HTTP.

Injectez une instance de `SimplePublisher` dans votre classe `MessageController`.

Créer une méthode `sendMessage` qui permet d'envoyer un message à Kafka.

Cette méthode doit être annotée avec `@PostMapping` et doit prendre en paramètre un objet `String`:

```java
@RestController
public class MessageController {
    private final SimplePublisherService simplePublisherService;

    public MessageController(SimplePublisherService simplePublisherService) {
        this.simplePublisherService = simplePublisherService;
    }

    @PostMapping("/message")
    public void sendMessage(String message) {
        simplePublisherService.sendMessage("my-topic", message);
    }
}
```

##### 3.1.3. Configuration de l'application

`KafkaTemplate` nécessite des configurations supplémentaires pour fonctionner.

Pour cela, nous allons créer un fichier `application.yml` dans le dossier `resources` de l'app __publisher__.

Ce fichier va contenir les configurations suivantes:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

> __Note__: par defaut spring créer le fichier `application.properties` mais nous allons utiliser le fichier `application.yml` pour avoir un format plus lisible.

`bootstrap-servers` correspond à l'adresse du Kafka sur lequel nous allons nous connecter.

### 3.2. Configuration du **consumer**

#### 3.2.1. Création du service

Créer une classe `SimpleConsumer`. Cette classe va permettre de consommer des messages à Kafka qui se trouve dans 
le topic `simple`.

Pour ce faire nous pouvons utiliser l'annotation `@KafkaListener` qui permet d'écouter un topic Kafka.

```java
@Service
public class SimpleConsumer {
    @KafkaListener(topics = "simple", groupId = "simple-group")
    public void consume(String message) {
        System.out.println("Message received: " + message);
    }
}
```

> __Note__: @KafkaListener prend en paramètre le nom du topic et le nom du groupe.

#### 3.2.2. Configuration de l'application

`@KafkaListener` nécessite des configurations supplémentaires pour fonctionner. Elle à besoin de connaître l'adresse du Kafka sur lequel elle doit se connecter.

Pour cela, nous allons créer un fichier `application.yml` dans le dossier `resources` de l'app __consumer__.

Ce fichier va contenir les configurations suivantes:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

Pour pouvoir lancer les deux applications en même temps, vous pouvez changer le port en ajoutant la configuration suivante:

```yaml
server:
  port: 8081
```

### 3.3. Lancement des applications

Lancez les deux applications en même temps ainsi que docker compose.

Pour tester, lancer une requête HTTP POST sur l'API `http://localhost:8080/message` avec un message en body.

Vous devriez voir le message dans la console du consumer.

## Exercice 2: Kafka Streams

Nous allons ajouter un nouveau service qui va consommer les messages du publisher et 
les transformer avant de les envoyer dans un nouveau topic.

![stream](./assets/pub-stream-cons.svg)

### 1. Configuration du **publisher**

#### 1.1. Configuration du publisher

Le publisher va maintenant devoir envoyer les messages vers le topic `text`.

Pour ce faire, nous allons ajouter une méthode `sendMessageToTextTopic` dans la classe `SimplePublisher`.

```java
class SimplePublisher {
    public void sendMessageToTextTopic(String message) {
        kafkaTemplate.send("text", message);
    }
}
```

#### 1.2. Modification de l'API HTTP

Nous allons aussi modifier l'API HTTP pour envoyer les messages vers le topic `text`.

```java
@RestController
public class MessageController {
    private final SimplePublisherService simplePublisherService;

    public MessageController(SimplePublisherService simplePublisherService) {
        this.simplePublisherService = simplePublisherService;
    }

    // ...
  
    @PostMapping("/text")
    public void sendMessage(String message) {
        simplePublisherService.sendMessageToTextTopic(message);
    }
}
```

### 2. Configuration du **stream**

Le service `stream` va consommer les messages du topic `text` et transformer les messages en comptant le nombre de mots.

#### 2.1. Création du Processor

Un `Processor` est une classe qui definit les opérations à effectuer sur les messages.

Créer une classe `WorldCountProcessor` annotée avec `@Component`.

```java
@Component
public class WorldCountProcessor {
    // TODO
}
```

Ajouter une méthode `buildPipeline` qui prend en paramètre un `KafkaStreamsBuilder`. Annotée cette méthode avec `@Autowired`
pour qu'elle soit appelée automatiquement au démarrage de l'application.

```java
@Component
public class WorldCountProcessor {
    @Autowired
    public void buildPipeline(KafkaStreamsBuilder builder) {
        // TODO
    }
}
```

Dance cette méthode nous allons créer un pipeline qui va consommer les messages du topic `text` et les transformer en comptant le nombre de mots.

Pour ce faire nous allons récupérer un `KStream` (pour KafkaStream) à partir du builder et sa méthode `stream`.

Elle prend en paramètre le nom du topic à consommer.

```java
@Component
public class WorldCountProcessor {
    @Autowired
    public void buildPipeline(KafkaStreamsBuilder builder) {
        KStream<String, String> stream = builder.stream("text");
        // TODO
    }
}
```

Nous allons ensuite utiliser les méthodes de stream pour transformer les messages.

Pour transformer le texte en tableau de mots, nous allons utiliser la méthode `flatMapValues` qui prend en paramètre 
une fonction qui prend en paramètre un message et retourne un `Iterable` de mots.

```java
@Component
public class WorldCountProcessor {
    @Autowired
    public void buildPipeline(KafkaStreamsBuilder builder) {
        KStream<String, String> stream = builder.stream("text");
        stream.flatMapValues(text -> Arrays.asList(text.split(" ")));
        // TODO
    }
}
```

Nous allons ensuite utiliser la méthode `groupBy` qui prend en paramètre une fonction qui prend en paramètre un mot et retourne une clé.

Cette méthode retourne un `KGroupedStream` qui permet de grouper les mots par clé.

```java
@Component
public class WorldCountProcessor {
    @Autowired
    public void buildPipeline(KafkaStreamsBuilder builder) {
        KStream<String, String> stream = builder.stream("text");
        stream.flatMapValues(text -> Arrays.asList(text.split(" ")));
        KGroupedStream<String, String> groupedStream = stream.groupBy(word -> word);
        // TODO
    }
}
```

Nous allons ensuite utiliser la méthode `count` qui retourne un `KTable` qui contient le nombre d'occurence de chaque mot.

```java
@Component
public class WorldCountProcessor {
    @Autowired
    public void buildPipeline(KafkaStreamsBuilder builder) {
        KStream<String, String> stream = builder.stream("text");
        stream.flatMapValues(text -> Arrays.asList(text.split(" ")));
        KGroupedStream<String, String> groupedStream = stream.groupBy(word -> word);
        KTable<String, Long> countTable = groupedStream.count();
        // TODO
    }
}
```

Il nous faut maintenant envoyer les messages vers le topic `words`.

Pour ce faire nous allons utiliser la méthode `toStream` qui retourne un `KStream` et la méthode `to` qui prend en paramètre le nom du topic.

```java
@Component
public class WorldCountProcessor {
    @Autowired
    public void buildPipeline(KafkaStreamsBuilder builder) {
        // Récupération du stream à partir du topic text
        KStream<String, String> stream = builder.stream("text");
        // Transformation du texte en tableau de mots
        stream.flatMapValues(text -> Arrays.asList(text.split(" ")));
        // Groupe les mots par clé (qui est le mot)
        KGroupedStream<String, String> groupedStream = stream.groupBy(word -> word);
        // Compte le nombre d'occurrences de chaque mot
        KTable<String, Long> countTable = groupedStream.count();
        // Convertit le nombre d'occurrences en string
        KTable<String, String> stringCountTable = countTable.mapValues(count -> ""+count);
        // Convertit le KTable en KStream pour pouvoir envoyer les messages vers le topic words
        countTable.toStream().to("words");
    }
}
```

#### 2.2. Configuration du stream

Si vous lancez l'application maintenant, vous allez avoir une erreur.

En effet, jusqu'à maintenant nous avons utilisé les configurations par défaut de Kafka. Les Streams ont besoin d'avoir
une configuration spécifique.

Pour configurer un client Kafka, nous passons par un @Bean. Dans le cas des streams, nous allons retourner un `KafkaStreamsConfiguration`.

Nous avons besoin de configurer 4 propriétés :
- `application.id` : l'id de l'application
- `bootstrap.servers` : l'adresse du serveur Kafka
- `serde.key` : la classe de sérialisation/désérialisation des clés
- `serde.value` : la classe de sérialisation/désérialisation des valeurs

```java
@Configuration
public class KafkaConfiguration {
    @Bean
    public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
        Map<String, Object> props = new HashMap<>();
        // Configuration de l'id de l'application
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream");
        // Configuration de l'adresse du serveur Kafka
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Configuration de la classe de sérialisation/désérialisation des clés
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // Configuration de la classe de sérialisation/désérialisation des valeurs
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return new KafkaStreamsConfiguration(props);
    }
}
```

L'Enum static `StreamsConfig` contient toutes les configurations possibles pour les streams.

Il existe aussi le même Enum pour les publisher et les consumers.

Nous avons aussi besoin d'activer KafkaStream sur notre application. Pour ce faire, nous allons ajouter l'annotation 
`@EnableKafkaStreams` sur notre classe de configuration.

```java
@Configuration
@EnableKafkaStreams
public class KafkaConfiguration {
    // ...
}
```

#### 3 Le Consumer

Nous allons maintenant créer un consumer qui va consommer les messages du topic `words`.

Nous allons utiliser la même classe que le consumer précédent.

```java
@Component
public class SimpleConsumer {
  @KafkaListener(topics = "simple", groupId = "simple-consumer")
  public void consume(String message) {
    System.out.println("Message reçu: " + message);
  }

  @KafkaListener(topics = "words", groupId = "text-consumer")
  public void consumeWords(){//TODO}
  }
  
}
```

Nous avons envoyé, depuis le service "stream", une valeur et sa clé sur le topic "words".
- La clé est le mot
- La valeur est le nombre d'occurrences du mot

Il nous faut donc demander à Kafka de nous donner la clé mais aussi la valeur.

Pour ce faire, nous allons utiliser les annotations `@Payload` et `@Header`.

```java
@Component
public class SimpleConsumer {
  // ...

  @KafkaListener(topics = "words", groupId = "text-consumer")
  public void consumeWords(
          
          @Payload String value, // Récupère la valeur du message avec l'anotation @Payload
          @Header(KafkaHeaders.RECEIVED_KEY) String key // Récupère la clé du message avec l'anotation @Header et la constante KafkaHeaders.RECEIVED_KEY
  ) {
    System.out.println("Message reçu: " + key + " - " + value);
  }
}
```

L'annotation `@Header` prend en paramètre le nom de la clé du header que l'on souhaite récupérer. Il est possible de récupérer
beaucoup d'autres informations dans les headers (comme le timestamp).

#### 4 Lancement de l'application

Vous pouvez maintenant lancer les 3 applications.

En envoyant une requête sur le service "publisher" et sur la route "POST /text", vous allez voir les messages s'afficher dans
le consumer.

```http request
POST http://localhost:8080/text
Content-Type: text/plain

Hello World
```

```text
Message reçu: Hello - 1
Message reçu: World - 1
```

> __Note :__ Il faut attendre quelques secondes avant de voir les messages s'afficher dans le consumer. Cela vient de l'utilusation
> d'une KTable qui est une solution StateFull et qui nécessite un temps de traitement.

## Exercice 3 : Utilisation d'Object

Pour l'instant, nous utilisons uniquement des strings pour envoyer et recevoir des messages. Nous allons maintenant utiliser
des objets.

Nous allons donc devoir sérialiser et désérialiser nos objets.

Nous allons utiliser la librairie `Jackson` pour cela. Elle sérialise et désérialise les objets en JSON.

### 1. Le Publisher

Nous allons créer un objet Message qui contient un id et un contenu.

```java
public class Message {
  private String content;
  private String auteur;

  public Message() {
  }

  // Getters et Setters
}
```

Nous allons maintenant ajouter une méthode dans le publisher qui va envoyer un objet Message.

Pour cela nous avons besoin d'un nouveau KafkaTemplate pour pouvoir envoyer des Messages.

```java
@Component
public class SimplePublisher {
    
    public final KafkaTemplate<String, String> kafkaStringTemplate;
    public final KafkaTemplate<String, Message> kafkaMessageTemplate;
    
    public SimplePublisher(KafkaTemplate<String, String> kafkaStringTemplate, KafkaTemplate<String, Message> kafkaMessageTemplate) {
        this.kafkaStringTemplate = kafkaStringTemplate;
        this.kafkaMessageTemplate = kafkaMessageTemplate;
    }
    
    // ...
  
    public void sendObject(Message message) {
      kafkaTemplate.send("object", message);
    }
}
```

N'utilisant plus la configuration par défaut, nous devons configurer nos KafkaTemplate.

Pour ce faire, nous allons créer une classe qui va contenir 4 Beans:
- Un Bean pour le KafkaTemplate de String (celui que nous utilisons déjà)
- Un Bean pour le KafkaTemplate de Message (celui que nous venons de créer)
- Un Bean pour PublisherFactory pour configurer le KafkaTemplate de String
- Un Bean pour PublisherFactory pour configurer le KafkaTemplate de Message

```java
@Configuration
public class KafkaConfiguration {
    // ...

  /**
   * Bean de configuration du ProducerFactory pour les messages de type String
   * Il permet de configurer la connexion au serveur Kafka et la sérialisation/désérialisation des messages
   * Dans notre cas nous utilisons la classe StringSerializer pour la sérialisation
   * @return le ProducerFactory configurer pour sérialiser les messages de type String
   */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

  /**
   * Bean de configuration du ProducerFactory pour les messages de type Message.
   * Il permet de configurer la connexion au serveur Kafka et la sérialisation/désérialisation des messages
   * Dans notre cas nous utilisons la classe JsonSerializer pour la sérialisation
   * @return le ProducerFactory configurer pour sérialiser les messages de type Message
   */
    @Bean
    public ProducerFactory<String, Message> messageProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

  /**
   * Bean de configuration du KafkaTemplate pour les messages de type String
   * Il prend en paramètre le ProducerFactory de String
   * @return le KafkaTemplate configuré pour envoyer des messages de type String
   */
    @Bean
    public KafkaTemplate<String, String> kafkaStringTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    /**
     * Bean de configuration du KafkaTemplate pour les messages de type Message
     * Il prend en paramètre le ProducerFactory de Message
     * @return le KafkaTemplate configuré pour envoyer des messages de type Message
     */
    @Bean
    public KafkaTemplate<String, Message> kafkaMessageTemplate() {
        return new KafkaTemplate<>(messageProducerFactory());
    }
}
```

Cette configuration permet de pouvoir utiliser deux types de KafkaTemplate dans notre application. Vous pouvez l'étendre
pour fournir autant de KafkaTemplate que vous voulez.

Le publisher est maintenant prêt à envoyer des objets Message sur le topic "object".

### 2. Le Stream

Nous allons faire de même pour le stream. Nous allons créer un nouveau stream qui va lire les messages de type Message
et les transformer dans un nouveau type de message (MessageInfo).

La classe message est la même que celle utilisée dans le publisher.

```java
public class Message {
  private String content;
  private String auteur;
  
  // Getters et Setters
}
```
La classe MessageInfo est la suivante :

```java
public class MessageInfo {
  private String content;
  private String auteur;
  private Integer size;
  
  // Getters et Setters
}
```

Nous devons maintenant créer un nouveau stream qui va lire les messages de type Message et les transformer en MessageInfo.

```java
@Component
public class SimpleStream {
    // ...

  @Autowired
  public void messagePipeline(StreamsBuilder builder){
    JsonSerde<Message> messageSerde = new JsonSerde<>(Message.class);
    KStream<String, Message> stream = builder.stream("object", Consumed.with(Serdes.String(), messageSerde));
    stream.mapValues(value -> {
      MessageInfo messageInfo = new MessageInfo();
      messageInfo.setAuthor(value.getAuthor());
      messageInfo.setMessage(value.getMessage());
      messageInfo.setSize(value.getMessage().length());
      return messageInfo;
    }).to("infos", Produced.with(Serdes.String(), new JsonSerde<>(MessageInfo.class)));
  }
}
```

En lancant l'application, un message d'erreur va apparaître dans la console. Il manque deux choses pour que le stream
fonctionne correctement.

Il faut définir un Serializer et un Deserializer par défaut dans la configuration de Kafka. Pour cela, modifions la classe
KafkaConfiguration comme suit :

```java
@Configuration
public class KafkaConfiguration {

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Serde (Serializer/Deserializer) par défaut pour les clés de type String
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // Serde (Serializer/Deserializer) par défaut pour les valeurs de type Message
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return new KafkaStreamsConfiguration(props);
    }
}
```

En relancant vous aurez maintenant un autre message d'erreur. Lorsque JsonSerde serialise un Object, il ajoute une métadonnée
qui donne la classe de l'objet. Hors quand le stream veut désérialiser le message, il ne trouve pas la classe Message (qui est 
celle dans le service "publisher"). Il lève donc une exception.

Pour résoudre ce problème, il faut lui spécifier de ne pas prendre en compte cette métadonnée. Pour cela, modifions la classe
WordCountProcessor comme suit :

```java
@Component
class WordCountProcessor {
  @Autowired
  public void messagePipeline(StreamsBuilder builder){
    JsonSerde<Message> messageSerde = new JsonSerde<>(Message.class);
    
    
    // On spécifie que la Serde ne doit pas prendre en compte la métadonnée
    messageSerde.ignoreTypeHeaders();
    
    
    KStream<String, Message> stream = builder.stream("object", Consumed.with(Serdes.String(), messageSerde));
    stream.mapValues(value -> {
      MessageInfo messageInfo = new MessageInfo();
      messageInfo.setAuthor(value.getAuthor());
      messageInfo.setMessage(value.getMessage());
      messageInfo.setSize(value.getMessage().length());

      return messageInfo;
    }).to("infos", Produced.with(Serdes.String(), new JsonSerde<>(MessageInfo.class)));
  }
}
```

Le service Stream peut maintenant être lancé sans erreurs. Il reçoit les messages de type Message du topic "object" et les
transforme en MessageInfo avant de les envoyer sur le topic "infos".

### 3. Le Consumer

Le consumer doit aussi être modifié pour pouvoir recevoir des messages de type MessageInfo. Nous allons donc créer une classe
MessageInfo.

```java
class MessageInfo {
  private String content;
  private String auteur;
  private Integer size;
  
  // Getters et Setters

  @Override
  public String toString() {
    return "MessageInfo{" +
            "message='" + message + '\'' +
            ", author='" + author + '\'' +
            ", size=" + size +
            '}';
  }
}
```

> __Note :__ Ajouter un toString() permet d'afficher les messages de manière plus lisible.

Nous allons ensuite créer un nouveau consumer qui va lire les messages de type MessageInfo dans la classe SimpleConsumer.

```java
@Component
class SimpleConsumer {

    // ...
  
    @KafkaListener(topics = "infos", groupId = "infos")
    public void listen(MessageInfo messageInfo) {
        System.out.println("Vous avez un nouveau message " + messageInfo);
    }
}
```

Si vous lancez l'application, vous verrez que vous avez des erreurs.

En effet, le consumer recoit des messages de type MessageInfo, mais il ne sait pas comment les désérialiser.

Il faut donc lui spécifier un Serde (Serializer/Deserializer) pour les valeurs de type MessageInfo.

Pour ce faire, comme pour les deux autres services, nous devons créer une classe KafkaConfiguration.

Il va définir 4 Beans :
- Un Bean pour la configuration du consumer (ConsumerFactory) qui deserialize les messages de type `String`
- Un Bean pour la configuration du consumer (ConsumerFactory) qui deserialize les messages de type `MessageInfo`
- Un Bean pour la création du gestionnaire de consumer (ConcurrentKafkaListenerContainerFactory) pour les messages de type `String`
- Un Bean pour la création du gestionnaire de consumer (ConcurrentKafkaListenerContainerFactory) pour les messages de type `MessageInfo`

Voici le Bean pour la configuration du consumer (ConsumerFactory) qui deserialize les messages de type `String`.

```java

@Configuration
public class KafkaConfiguration {

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
    // Configuration du Deserializer pour les valeurs de type String
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(config);
  }
  
  // ...

}
```

Voici le Bean pour la configuration du consumer (ConsumerFactory) qui deserialize les messages de type `MessageInfo`.

```java
@Configuration
class KafkaConfiguration {
  // ...
  @Bean
  public ConsumerFactory<String, MessageInfo> messageConsumerFactory() {
    Map<String, Object> config = new HashMap<>();

    // Création d'un Deserializer pour les valeurs de type MessageInfo
    JsonDeserializer<MessageInfo> deserializer = new JsonDeserializer<>(MessageInfo.class);
    // Comme pour le Stream, on spécifie que la Serde ne doit pas prendre en compte la métadonnée de Types
    deserializer.ignoreTypeHeaders();

    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    // On Spécifie le Deserializer pour les valeurs de type MessageInfo
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

    // On retourne le ConsumerFactory
    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
  }
    // ...
}
```

Voici le Bean pour la création du gestionnaire de consumer (ConcurrentKafkaListenerContainerFactory) pour les messages de type `String`.

```java
@Configuration
class KafkaConfiguration {
  // ...
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

}
```

Voici le Bean pour la création du gestionnaire de consumer (ConcurrentKafkaListenerContainerFactory) pour les messages de type `MessageInfo`.

```java
@Configuration
class KafkaConfiguration {
    // ...
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageInfo> messageKafkaListenerContainerFactory(
            ConsumerFactory<String, MessageInfo> messageConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, MessageInfo> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(messageConsumerFactory);
        return factory;
    }
}
```

Nous pouvons maintenant modifier le consumer pour qu'il puisse recevoir des messages de type MessageInfo.

```java
@Component
class SimpleConsumer {

    // ...

    @KafkaListener(
            topics = "infos", 
            groupId = "infos", 
            containerFactory = "messageKafkaListenerContainerFactory") // On spécifie le containerFactory en fonction du nom du Bean
    public void listen(MessageInfo messageInfo) {
        System.out.println("Vous avez un nouveau message " + messageInfo);
    }
}
```

Si vous relancez l'application, vous verrez que les messages sont bien reçus.

### Test de l'application

Vous pouvez maintenant tester l'application.

La requête suivante permet d'envoyer un message de type String.

```http request
POST http://localhost:8080/message
Content-Type: plain/text

Hello World, ceci est un message de type String
```

Le message sera envoyé sur le topic `simple` et sera reçu par le consumer qui l'affichera dans la console.

La requête suivante permet d'envoyer un message de type MessageInfo.

```http request
POST http://localhost:8080/text
Content-Type: plain/text

Hello World, ceci est un message qui doit être traité par un compteur de mots
```

Vous verrez dans la console du consumer que le message a été reçu et traité.

```text
Hello 1
World 1
ceci 1
est 1
un 1
message 1
qui 1
doit 1
être 1
traité 1
par 1
un 2
compteur 1
de 1
mots 1
``` 

Vous pouvez envoyez des messages au format json en utilisant la requête suivante.

```http request
POST http://localhost:8080/object
content-type: application/json

{
  "message": "Hello World, ceci est un message de type MessageInfo",
  "author": "John Doe"
}
```

Vous verrez dans la console du consumer que le message a été reçu et traité.

```text
MessageInfo{message='Hello World, ceci est un message de type MessageInfo', author='John Doe', size=52}
```

## Conclusion

Cet exercice vous a permis de découvrir comment utiliser les Publishers, Consumer et les Streams de Kafka avec Spring Boot
et comment les configurer pour qu'ils puissent traiter des messages de type String et de type MessageInfo.

Vous trouverez la documentation officielle de spring kafka [https://spring.io/projects/spring-kafka](https://spring.io/projects/spring-kafka)
