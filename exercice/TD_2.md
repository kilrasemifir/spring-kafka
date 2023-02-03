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
- avoir suivi le [TD_1](TD.md)

## Objectifs

Comprendre comment faire une jointure entre deux topics Kafka avec Kafka Streams.

## Exercice 4: Jointure entre deux topics

Nous allons dans cet exercice faire une jointure entre deux topics Kafka pour obtenir un nouveau topic.
- Le premier topic `Person` contient les informations d'une personne
- Le second topic `Command` contient les informations d'une commande
- Le nouveau topic `bills` contient les informations d'une facture entre une personne et une commande

### 1. Le producer

Nous allons modifier le producer pour qu'il produise des données dans les deux topics `Person` et `Command`.

#### 1.1. Le topic `Person`

Créons une classe `Person` qui contient les informations d'une personne.

```java
public class Person {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    
    // Getters and setters
}
```

Il faut maintenant configurer le producer. Pour que la classe KafkaConfig ne devienne pas trop grosse,
nous allons créer une classe `PersonProducerConfig` qui va contenir la configuration du producer pour le topic `Person`.

```java
@Configuration
public class PersonProducerConfiguration {

    @Bean
    public ProducerFactory<String, Person> personProducerFactory() {
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

    @Bean
    public KafkaTemplate<String, Person> kafkaPersonTemplate(ProducerFactory<String, Person> personProducerFactory) {
        return new KafkaTemplate<>(personProducerFactory);
    }
}
```

Nous allons maintenant créer un service `PersonProducer` qui va produire des données dans le topic `Person`.

```java
@Component
public class PersonProducer {

    private final KafkaTemplate<String, Person> kafkaTemplate;

    public PersonProducer(KafkaTemplate<String, Person> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPerson(Person person) {
        kafkaTemplate.send("person", person);
    }
}
```

Il nous faut maintenant un controller pour envoyer des données dans le topic `Person`.

```java
@RestController
@RequestMapping("/persons")
public class PersonController {

        private final PersonProducer personProducer;

        public PersonController(PersonProducer personProducer) {
            this.personProducer = personProducer;
        }

        @PostMapping
        public void publishPerson(@RequestBody Person person) {
            personProducer.sendPerson(person);
        }
}
```

#### 1.2. Le topic `Command`

Nous allons faire de même pour le topic `Command`.

Créons une classe `Command` qui contient les informations d'une commande.

```java
public class Command {
    private String id;
    private String name;
    private String description;
    private String idPerson;

    // Getters and setters

}
```

Créons la classe `CommandProducerConfig` qui va contenir la configuration du producer pour le topic `Command`.

```java
@Configuration
public class CommandProducerConfiguration {

    @Bean
    public ProducerFactory<String, Command> commandProducerFactory() {
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

    @Bean
    public KafkaTemplate<String, Command> kafkaCommandTemplate(ProducerFactory<String, Command> commandProducerFactory) {
        return new KafkaTemplate<>(commandProducerFactory);
    }
}
```

Créons le service `CommandProducer` qui va produire des données dans le topic `Command`.

```java
@Component
public class CommandProducer {

    private final KafkaTemplate<String, Command> kafkaTemplate;

    public CommandProducer(KafkaTemplate<String, Command> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCommand(Command command) {
        kafkaTemplate.send("command", command);
    }
}
```

Créons le controller pour envoyer des données dans le topic `Command`.

```java
@RestController
@RequestMapping("/commands")
public class CommandController {

    private final CommandProducer commandProducer;

    public CommandController(CommandProducer commandProducer) {
        this.commandProducer = commandProducer;
    }

    @PostMapping
    public void publishCommand(@RequestBody Command command) {
        commandProducer.sendCommand(command);
    }
}
```

### 2. Le Stream

Le Stream doit faire une jointure entre les deux topics `Person` et `Command` pour obtenir un nouveau topic `bills`.

#### 2.1. Le topic `bills`

Nous devons créer les classes `Person` et `Command` pour pouvoir les utiliser dans le Stream.

```java
public class Person {
    private String id;
    private String firstName;
    private String lastName;
    private String email;

    // Getters and setters
}
```

```java
public class Command {
    private String id;
    private String name;
    private String description;
    private String idPerson;

    // Getters and setters
}
```

Créons une classe `Bill` qui contient les informations d'une facture.

```java
public class Bill {
    private Person person;
    private Set<Command> commands = new HashSet<>();

    // Getters and setters
}
```

#### 2.2. Le Stream

Nous devons faire une jointure entre les deux topics `Person` et `Command` pour obtenir un nouveau topic `bills`.

Il nous faut donc récupérer les deux Streams `personStream` et `commandStream` 
et les joindre pour obtenir le Stream `billStream`.

Pour cela, nous devons créer une nouvelle class `BillProcessor` qui va contenir la logique de jointure.

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // TODO
    }
}
```

Il nous faut récupérer les deux Streams `personStream` et `commandStream` et les joindre pour obtenir le Stream `billStream`.

Il nous faut configurer les Serdes pour les deux Streams `personStream` et `commandStream` sans oublier de ne pas
prendre en compte les headers.

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // Serde pour les objets Person
        JsonSerde<Person> personSerde = new JsonSerde<>(Person.class);
        personSerde.ignoreTypeHeaders();

        // Serde pour les objets Command
        JsonSerde<Command> commandSerde = new JsonSerde<>(Command.class);
        commandSerde.ignoreTypeHeaders();

        // Serde pour les objets Bill
        JsonSerde<Bill> billSerde = new JsonSerde<>(Bill.class);

        // Création des deux Streams
        KStream<String, Person> personStream = builder.stream("persons", Consumed.with(Serdes.String(), personSerde));
        KStream<String, Command> commandStream = builder.stream("commands", Consumed.with(Serdes.String(), commandSerde));
    }
}

```

Pour faire correctement des jointures, nous allons convertir nos Streams en Tables.

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // ...
        KStream<String, Person> personStream = builder.stream("persons", Consumed.with(Serdes.String(), personSerde));
        KStream<String, Command> commandStream = builder.stream("commands", Consumed.with(Serdes.String(), commandSerde));

        KTable<String, Person> personTable = personStream.toTable();
        KTable<String, Command> commandTable = commandStream.toTable();

    }
}
```

Il nous faut maintenant faire une fonction de jointure entre les deux tables:
    
```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // ...
        
        KTable<String, Person> personTable = personStream.toTable();
        KTable<String, Command> commandTable = commandStream.toTable();

        ValueJoiner<Person, Command, Bill> joiner = (person, command) -> {
            Bill bill = new Bill();
            bill.setPerson(person);
            bill.getCommands().add(command);
            return bill;
        };
    }
}
```

Et nous pouvons maintenant appelé la fonction de jointure de la table `personTable` avec la table `commandTable`.

De la même manière que le SQL, il existe plusieurs types de jointures:

- `leftJoin` : jointure gauche
- `rightJoin` : jointure droite
- `join` : jointure interne

Dans notre cas, nous voulons faire une `leftJoin` car nous voulons récupérer les personnes qui n'ont pas passé de commande.

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // ...
        ValueJoiner<Person, Command, Bill> joiner = (person, command) -> {
            // ...
        };

        KTable<String, Bill> billTable = personTable.leftJoin(commandTable, joiner);
    }
}
```

La `KTable` va alors contenir les personnes et les commandes. Il y aura plusieurs entrées pour une même personne 
car une personne peut avoir plusieurs commandes.

Pour s'assurer que nous n'aurons qu'une seule entrée par personne, nous devons faire un `groupBy` sur la `KTable`:

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // ...
        KTable<String, Bill> billTable = personTable.leftJoin(commandTable, joiner);

        KGroupedTable<String, Bill> groupedTable = billTable.groupBy((key, bill) -> {
            return KeyValue.pair(""+bill.getPerson().getId(), bill);
        });
    }
}
```

Puis effectuer une réduction sur la `KGroupedTable` pour avoir une seule entrée par personne:

Les `reduces` prennent deux paramètres:
- `adder`: un reducer qui va ajouter une nouvelle entrée à la table
- `substracor`: un reducer qui va soustraire une entrée de la table

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // ...
        KGroupedTable<String, Bill> groupedTable = billTable.groupBy((key, bill) -> {
            return KeyValue.pair(""+bill.getPerson().getId(), bill);
        });

        Reducer<Bill> add = (bill1, bill2) -> {
            bill1.getCommands().addAll(bill2.getCommands());
            return bill1;
        };

        Reducer<Bill> remove = (bill1, bill2) -> {
            bill1.getCommands().removeAll(bill2.getCommands());
            return bill1;
        };

        KTable<String, Bill> groupedBill = groupedTable.reduce(add, remove);
    }
}
```

Nous pouvons maintenant publier le résultat dans un nouveau topic `bills`:

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        // ...
        KTable<String, Bill> groupedBill = groupedTable.reduce(add, remove);
        groupedBill.toStream().to("bill", Produced.with(Serdes.String(), billSerde));
    }
}
```

Ce qui nous donne le code complet:

```java
@Component
public class BillProcessor {

    @Autowired
    public void billPipeline(StreamsBuilder builder) {
        JsonSerde<Person> personSerde = new JsonSerde<>(Person.class);
        personSerde.ignoreTypeHeaders();

        JsonSerde<Command> commandSerde = new JsonSerde<>(Command.class);
        commandSerde.ignoreTypeHeaders();

        JsonSerde<Bill> billSerde = new JsonSerde<>(Bill.class);

        KStream<String, Person> personStream = builder.stream("persons", Consumed.with(Serdes.String(), personSerde));
        KStream<String, Command> commandStream = builder.stream("commands", Consumed.with(Serdes.String(), commandSerde));

        KTable<String, Person> personTable = personStream.toTable();
        KTable<String, Command> commandTable = commandStream.toTable();

        ValueJoiner<Person, Command, Bill> joiner = (person, command) -> {
            Bill bill = new Bill();
            bill.setPerson(person);
            bill.getCommands().add(command);
            return bill;
        };

        KTable<String, Bill> billTable = personTable.leftJoin(commandTable, joiner);
        KGroupedTable<String, Bill> groupedTable = billTable.groupBy((key, bill) -> {
            return KeyValue.pair(""+bill.getPerson().getId(), bill);
        });

        Reducer<Bill> add = (bill1, bill2) -> {
            bill1.getCommands().addAll(bill2.getCommands());
            return bill1;
        };

        Reducer<Bill> remove = (bill1, bill2) -> {
            bill1.getCommands().removeAll(bill2.getCommands());
            return bill1;
        };

        KTable<String, Bill> groupedBill = groupedTable.reduce(add, remove);


        groupedBill.toStream().to("bill", Produced.with(Serdes.String(), billSerde));
    }
}
```

### 2.3. Le Consumer

Nous allons maintenant créer un consumer qui va lire le topic `bills` et afficher le résultat dans la console.

Il nous faut d'abord créer les classes `Person`, `Command` et `Bill`:

```java
public class Person {
    private int id;
    private String name;
    private String email;

    // getters and setters
}

public class Command {
    private int id;
    private String name;
    private double price;

    // getters and setters
}

public class Bill {
    private Person person;
    private List<Command> commands = new ArrayList<>();

    // getters and setters
}
```

Comme pour les `MessageInfo` de l'exercice précédent, nous devons configurer un ListenerContainerFactory pour que le consumer
puisse lire les messages de type `Bill`:

```java
@Configuration
public class BillConsumerConfiguration {
    @Bean
    public ConsumerFactory<String, Bill> billConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        JsonDeserializer<Bill> deserializer = new JsonDeserializer<>(Bill.class);
        deserializer.ignoreTypeHeaders();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Bill> billKafkaListenerContainerFactory(
            ConsumerFactory<String, Bill> billConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Bill> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(billConsumerFactory);
        return factory;
    }
}
```

Nous pouvons maintenant créer le consumer:

```java
@Component
public class BillConsumer {

    @KafkaListener(topics = "bill", groupId = "bill-consumer", containerFactory = "billKafkaListenerContainerFactory")
    public void listen(Bill bill) {
        System.out.println(bill);
    }
}
```

### 2.4. Lancement de l'application

Vous pouvez lancez les 3 applications en même temps.

Lancer les requêtes suivantes:

```bash
POST http://localhost:8080/persons
Content-Type: application/json

{
    "id":1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@email.com"
}
```

et 

```bash
POST http://localhost:8080/commands
Content-Type: application/json

{
    "id":1,
    "name": "Pomme",
    "description": "Un fruit"
}
```
