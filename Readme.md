Des producteurs et des consommateurs se déchaînent sur une pauvre boîte. Toutefois, pilotés par des threads, ces objets sont mis en attente lorsqu'ils ne peuvent utiliser correctement la boîte.

### Préliminaires

Nous allons simuler des producteurs qui produisent des valeurs qu'ils rangent dans une boîte commune, et des consommateurs qui consomment ces valeurs après les avoir prises dans cette boîte. Tout l'intérêt de la chose vient de ce que l'on ne peut stocker qu'une valeur à la fois dans la boite et que le nombre de producteurs est différent de celui des consommateurs : certains devront attendre que les conditions deviennent favorables avant de pouvoir travailler.

Voici à quoi devrait ressembler votre application une fois terminée, lorsqu'il y a 3 producteurs et 2 consommateurs :

L'exécution s'est terminée par la suspension des producteurs qui ne peuvent plus produire puisque la boite est pleine car les consommateurs sont tous morts et qu'ils ne peuvent donc plus consommer. Les producteurs bloqués ont ensuite été exterminés.

Entre deux exécutions, l'application tire un trait de séparation avant de redémarrer (non visible sur la photo).

### Présentation des paquetages :

prodcons contient la classe racine de l'application : Main.
prodcons.model contient l'interface Box qui spécifie les boites, et sa classe d'implémentation non thread safe UnsafeBox. Elle contient aussi l'interface Actor qui propose une spécification globale pour les producteurs et les consommateurs (que nous appellerons acteurs lorsque nous ne voudrons pas les distinguer). On y trouve enfin l'interface ProdConsModel qui spécifie le modèle de notre application, et sa classe d'implémentation SdProdConsModel.
prodcons.model.actor contient les deux classes d'implémentation de Actor. Le code commun à tous les acteurs est avantageusement factorisé dans une classe abstraite.
prodcons.util.event contient la classe SentenceEvent qui définit les événements porteurs des phrases déclamées par les acteurs, l'interface SentenceListener des écouteurs qui sont associés à ce type d'événements, et la classe SentenceSupport qui contient une version ad'hoc du mécanisme de gestion de ces écouteurs.
prodcons.util contient deux classes utilitaires, l'une (Formatter) pour formatter les messages affichés dans l'application, l'autre (Logger) pour afficher sur la console des informations sur l'exécution de l'application.
prodcons.gui contient la classe ProdCons qui code l'application graphique mettant en jeu plusieurs acteurs...
Les boites et les acteurs
Une boîte ne garantit pas une utilisation sans erreur dans un contexte multi-threads, elle n'est pas thread-safe d'où le nom de son type : UnsafeBox. Elle contient à chaque instant au plus une valeur.

Un acteur est un objet qui agit sur une boite. L'action d'un acteur sur la boite consiste à remplir (fill()) celle-ci lorsqu'il s'agit d'un producteur, ou à la vider (clear()) lorsqu'il s'agit d'un consommateur. Il est important de noter dès maintenant qu'au sein du modèle ProdConsModel, il n'existera qu'une seule boite partagée entre tous les producteurs et tous les consommateurs. De la sorte, un acteur sera parfois obligé d'attendre que la boite satisfasse certaines conditions avant qu'il puisse l'utiliser.

Un acteur commence à agir lorsqu'on lui envoie le message start() ; il démarre alors un thread interne qui boucle sur l'utilisation de la boite (useBox()) lorsque c'est possible (canUseBox()), getMaxIterNb() fois ou moins si on l'interrompt prématurément. Les méthodes en rapport avec la manipulation de la boite ont une sémantique qui est fonction du type précis de l'acteur : un consommateur vide la boite, alors qu'un producteur remplit la boite. Bien entendu, s'il n'est momentanément pas possible pour un acteur d'utiliser la boite, son thread interne devra se mettre en attente...

Un acteur est vivant (isAlive()) si le thread qui l'anime a été démarré et n'a pas encore terminé son exécution (comme la méthode isAlive() des contrôleurs de threads). La méthode isAlive() d'un acteur ne doit pas être confondue avec sa méthode isActive() qui rend compte de l'état d'activité du thread interne de l'acteur : est-il en cours d'exécution (isActive()) ou bien est-il en attente d'utilisation de la boite ou a-t-il terminé son exécution (!isActive()) ?

### Des acteurs bavards

Durant toute son activité, un acteur “parle” pour décrire ce qu'il fait. Un SentenceEvent, en plus de sa source, véhicule un message (je vous rappelle que sentence en anglais peut se traduire en français par le mot phrase), ce qui permet aux acteurs de parler par notifications. Il suffit alors de les observer (= enregistrer un SentenceListener sur un acteur) pour être notifié de leur histoire et réagir en conséquence. La classe SentenceSupport joue un rôle identique à celui de PropertyChangeSupport que vous connaissez bien.

Les messages générés par un acteur sont les suivants :

« Naissance » au démarrage de l'acteur.
« Début de l'étape x » au début du x-ième tour de boucle (il y aura au plus getMaxIterNb() tours de boucle).
« Demande le verrou » juste avant que le thread interne de l'acteur demande le verrou de la boite pour pouvoir ensuite se mettre en attente.
« Acquiert le verrou » juste après que le thread interne de l'acteur ait obtenu le verrou de la boite.
« Rentre dans la salle d'attente » juste avant que le thread interne de l'acteur soit mis en attente par la boite.
« Sort de la salle d'attente » juste après que le thread interne de l'acteur soit relâché par la boite.
« Libère le verrou » juste avant que le thread interne de l'acteur ne libère le verrou de la boite.
« Fin de l'étape x » à la fin du x-ième tour de boucle.
« Mort subite » si l'acteur a été interrompu avant la fin normale de son exécution.
« Mort naturelle » si le thread interne de l'acteur a terminé normalement son code cible (ie. a exécuté sa boucle sans interruption).
« box --> y » lorsqu'un consommateur consomme la valeur y qui se trouve dans la boîte.
« box <-- y » lorsqu'un producteur produit la valeur y qu'il stocke dans la boîte.
Pour que l'on y voie clair dans l'affichage des différents threads, vous indenterez systématiquement les messages ci-dessus en fonction de l'acteur qui en est l'auteur (voir les clichés de l'application). C'est pourquoi, afin de séparer le fonctionnement propre aux acteurs du code gérant l'indentation de l'affichage, j'ai défini la classe Formatter dédiée à la mise en forme des messages. Chaque acteur possède alors son propre formateur, configuré pour calculer systématiquement l'indentation nécessaire et le préfixe des messages de cet acteur.

### Travail à faire dans la classe AbstractActor fournie

La méthode fireSentenceSaid réalise la notification des SentenceListener. Lisez bien le commentaire qui précède cette méthode et codez-la.
Des acteurs... actifs !
Vous l'avez compris, chaque acteur est doté d'un thread privé dont le code cible utilise la boite partagée en boucle (finie). Vous pouvez d'ailleurs regarder le code de AbstractActor, vous y trouverez un attribut thread de type Thread : c'est le thread interne de l'acteur. La méthode AbstractActor.start() est appelable lorsque le thread interne est bien arrêté. Dans ces conditions, elle redémarre l'acteur en lui affectant un nouveau thread, puis en démarrant ce dernier.

Le code cible du thread interne est une instance de AbstractActor.TaskCode mémorisée à la création de l'acteur dans son attribut task. La classe membre TaskCode implémente Runnable pour définir le code qui boucle sur l'utilisation de la boite par l'acteur.

Cette boucle d'utilisation repose sur deux méthodes abstraites et protégées :

boolean canUseBox() : indique si l'acteur peut utiliser la boite.
void useBox() : utilise la boite selon la nature de l'acteur.
Ces deux méthodes constituent la partie variable et adaptable de la boucle d'utilisation de chaque type d'acteur (consultez les classes StdConsumer et StdProducer).

Voici maintenant le schéma de principe d'un appel à TaskCode.run(). À la lecture du code fourni en ressource, vous vous rendrez compte que la méthode run est déjà partiellement codée : il ne manque plus que le corps de oneStep(), qui correspond au cadre sur fond bleu à l'intérieur de la boucle principale sur le schéma ci-dessous. Attention toutefois :

aucune espèce de synchronisation n'est indiquée sur ce diagramme ;
rien n'y est dit quant à l'activité de l'acteur ;
rien n'y est dit quant à la survenue d'une InterruptedException lors de l'exécution de wait().
taskrun.svg

Une précision pour finir, la politique d'interruption dans la méthode TaskCode.run() est une sortie la plus rapide possible pour n'émettre qu'un dernier message dans ce cas : « Mort subite ».

Travail à faire dans la classe AbstractActor fournie
Complétez la méthode oneStep() dans la classe TaskCode et assurez-vous que tous les messages de l'acteur qui décrivent ce qu'il fait sont bien émis.
Des acteurs que l'on peut arrêter
La méthode interruptAndWaitForTermination() interrompt l'activité du thread interne de l'acteur et bloque le thread appelant dans l'attente de la terminaison du thread de l'acteur avant de poursuivre.

Son algorithme consiste à :

interrompre thread, au cas où il serait en attente dans un appel bloquant
bloquer le thread courant (celui qui exécute interruptAndWaitForTermination(), et qui ne devra donc pas être le thread de l'acteur lui-même) dans l'attente de la terminaison du thread de l'acteur.
Travail à faire dans la classe AbstractActor fournie
Codez la méthode interruptAndWaitForTermination().
Retouchez enfin les attributs des classes AbstractActor et TaskCode en fonction de leur catégorie de partage (selon que ce sont des variables partagées ou non, mutables ou non, nécessitant une gestion atomique ou pas ; utilisez l'outil qui vous a été donné au TD 5).
Le modèle de l'application
Le modèle de l'application est spécifié dans l'interface ProdConsModel et une classe d'implémentation standard (incomplète) StdProdConsModel est fournie.

Un tel modèle possède une boite (box()) et des acteurs auxquels on accède individuellement par consumer(int) (pour les consommateurs) et producer(int) (pour les producteurs). Il indique à chaque instant s'il est gelé (isFrozen()), c'est-à-dire si tous les producteurs sont morts alors qu'il reste des consommateurs vivants et tous bloqués, ou bien la situation réciproque : si tous les consommateurs sont morts alors qu'il reste des producteurs vivants et tous bloqués. Il indique aussi s'il est en cours d'exécution (isRunning() == true) ou pas (== false).

Au niveau des commandes, on peut démarrer un modèle dont aucun acteur n'est vivant (start()) ou tuer tous les acteurs encore vivants d'un modèle (stop()).

#### Propriétés du modèle

Comme vous pourrez le constater en examinant le code de ProdConsModel, ce type devra posséder trois propriétés, de nom sentence, running et frozen. Je dis « devra » car la classe StdProdConsModel fournie n'en contient qu'une pour l'instant : running. Et encore, de façon incomplète puisque les méthodes start() et stop() ne sont pas données, et qu'elles déterminent la valeur de cette propriété au cours du temps.

Par ailleurs, les deux autres propriétés (sentence et frozen) ne sont pas implantées du tout, et ce sera à vous de le faire.

La propriété sentence est de type String. Cette propriété sera liée et à notification forcée, mais elle sera inaccessible aussi bien en lecture qu'en écriture, ce qui signifie qu'il n'y a pas de méthode publique getSentence ni setSentence au niveau du modèle. Les valeurs de cette propriété, qui ne sont pas directement connaissables, ne sont donc accessibles au niveau de l'application que par l'utilisation de PropertyChangeListeners appropriés.

Comment faire pour émettre des notifications lors du changement de valeur de cette propriété ? Nous avons vu que les acteurs sont source de SentenceEvents. Il faudra donc capter ces événements émis par les acteurs et les traduire aussitôt en notifications de changement de valeur de la propriété sentence émises par le modèle. Pour cela, vous définirez à l'intérieur du modèle un observateur qui agira comme un relai : il observe les phrases émises par tout acteur, et les retransmet immédiatement sous forme de notification de changement de valeur de la propriété sentence du modèle.

Passons ensuite à la propriété frozen, de type boolean. Cette propriété sera liée et accessible en lecture uniquement. Cela ne veut pas dire qu'il n'existe pas de méthode setFrozen, cela signifie que cette dernière est privée.

Par définition, un modèle est gelé si, et seulement si, il n'y a que des acteurs vivants mais bloqués dans l'une des deux catégories, tandis que tous ceux de l'autre catégorie sont morts. Pour déterminer les variations de la propriété frozen, il faudra donc brancher un PropertyChangeListener sur chaque acteur, pour pouvoir observer les variations de leur propriété active.

Vous définirez au sein du modèle une classe interne FrozenDetector qui implémente PropertyChangeListener, votre code sera ainsi plus lisible. Une seule instance de cette classe sera créée, qui sera associée à tous les acteurs. Les notifications reçues par cet observateur provenant de tous les acteurs, elles sont susceptibles d'être exécutées sur n'importe quel thread d'acteur, et à n'importe quel moment. À chaque notification reçue, l'observateur doit calcule la valeur de frozen et terminer ce calcul par une notification de changement de valeur pour la propriété frozen du modèle. Vous ferez donc bien attention que ces notifications soient faites de manière atomique car le calcul de la nouvelle valeur de frozen ne peut pas être interrompu par les acteurs.

Après avoir effectué ce calcul, si notre FrozenDetector détecte que le modèle est gelé, il doit stopper tous les acteurs vivants du modèle.

### Travail à faire dans la classe StdProdConsModel fournie

Implémentez dans cette classe la propriété sentence dont les changements de valeur sont notifiés suite à l'observation des phrases émises par les acteurs.
Implémentez dans cette classe la propriété frozen dont les changements de valeur sont notifiés suite aux déductions faites lors de chaque changement de valeur de la propriété active des différents acteurs. Pour cela il vous faudra :
compléter isFrozen() (et ajouter un attribut) ;
définir une méthode privée setFrozen() (n'oubliez pas que la propriété est liée) ;
définir une classe interne FrozenDetector permettant l'observation de la propriété active des acteurs afin de calculer les valeurs de la propriété frozen du modèle, et de stopper éventuellement les acteurs si le modèle est gelé ;
associer une instance de cette classe à chacun des acteurs (la même pour tous les acteurs).
Codez maintenant le corps de la méthode start(), à cette occasion il faudra :
effacer le contenu de la boite partagée entre tous les acteurs ;
réinitialiser le compteur de temps du formatteur ;
réinitialiser les propriétés frozen et running ;
puis démarrer chaque acteur un par un.
Codez ensuite le corps de la méthode stop() qui termine tous les acteurs encore en vie puis réactualise la propriété running.
Retravaillez la déclaration des attributs des classes StdProdConsModel et FrozenDetector à l'aide de l'outil décisionnel donné au TD 5.
Test de l'application
Lorsque vous lancez l'application, vous voyez deux boutons. Vous connaissez bien le premier, qui va démarrer le modèle. Le second bouton vous permet d'interrompre le modèle en cours d'exécution.

Il ne vous reste plus qu'à tester votre application. Assurez-vous de la tester avec un nombre de producteurs différent de celui des consommateurs, histoire d'être sûr que le modèle va finir gelé... Horreur ! Ca ne fonctionne pas !!!

Sauriez-vous déterminer d'où vient le problème ? Pour cela il vous faudra découvrir l'identité du thread qui stoppe tous les acteurs...

Quoi qu'il en soit, le remède est simple : définir un thread nettoyeur qui s'occupera de stopper les acteurs encore vivants.

### Travail à faire dans la classe StdProdConsModel fournie

Codez une classe interne privée EraserTask qui implémente Runnable et dont la méthode run() consiste à détruire tous les acteurs encore vivants.
Rectifiez la méthode stop() du modèle en utilisant cette nouvelle classe interne.
