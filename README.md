# TinyLDF

## Projet M1 - données massives et cloud 

Reproduction du serveur de fragments de données simples liés de Wikidata : https://query.wikidata.org/bigdata/ldf.  
C'est un simple serveur LDF qui permet de traiter les requêtes de modèles triples et les pages de retour des résultats dans RDF. 

## Dataset

Le dataset provient de [notre projet de web sémantique](https://github.com/JulienBacquart/WebSemantique) et contient des données relatives aux Jeux Olympiques d'été 2024.  

Il s'agit de la sémantisation du dataset [OlympicsGoNUTS](https://github.com/EDJNet/OlympicsGoNUTS).  
Ce dataset comprend 40 935 triplets, fournissant des données sur :  
- Les médailles par discipline, pays et par athlète.
- Les données NUTS relatives aux lieux de naissance des athlètes, notamment la population et le PIB.  

Pour plus de détails sur les données originales, consultez : [OlympicsGoNUTS](https://edjnet.github.io/OlympicsGoNUTS/)

## Analyse des temps d'exécution

Dans cette analyse, nous avons tester 7 types de réquêtes différentes en les exécutant fois. On a tester les requêtes avec aucunes donnée, un sujet, un prédicat, un objet, combinaison de sujet/prédicat/objet, un sujet et un objet, et pour finir un prédicat et un graph. 

| | Rien | Sujet | Prédicat | Objet | SPO | SO | P/Graph |
| - |-----|-------|----------|-------|-----|----| ------- |
| 1 | 128 | 133 | 107 | 185 | 104 | 78 | 90 |
| 2 | 155 | 127 | 113 |	150	| 120 | 98 | 83 |
| 3 | 101 |	191 | 145 |	131	| 105 | 72 | 135 |
| 4 |  95 |	 79 | 105 |	 89	| 100 | 79 | 99 |
| 5 |  80 |	 72 |  90 |	 80	| 111 | 90 | 84 |

On a regardé la moyenne et l'écart-type des résultats obtenus 

|         | Rien | Sujet | Prédicat | Objet | SPO | SO | P/Graph |
| ------- |------|-------|----------|-------|-----|----| ------- |
| Moyenne | 111,8 | 120,4 | 112,0 | 127,0 | 108,0 | 83,4 | 98,2 |
| Écart type | 29,7	| 48,1 | 20,3 |  43,5 |   7,8 | 10,4 | 21,5 |

Si on regarde les écart-types, on remarque que les temps d'exécution ont une faible variabilité. Les temps d'exécution ci-dessus sont en millisecondes. On peut en déduire que le temps d'exécution est proportionnel au nombre de résultats produits et indépendant de la taille des données. 

## Auteurs (M1 DS)

| Groupe       |
| ---------------- |
| Maëlle LE LANNIC |
| Thomas GIRAUD   |
| Julien BACQUART  |
