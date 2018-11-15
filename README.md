# account-manager
RESTful API for money transfers between accounts

Account api
```
POST /accounts - create account
curl -X POST  "localhost:4567/accounts?amount=10.123" | json_pp
{
   "amount" : 10.123,
   "id" : 1
}

GET /accounts/:accountId - get account-info
curl "localhost:4567/accounts/1" | json_pp
{
   "id" : 1,
   "amount" : 10.123
}

PUT /accounts/:accountId/deposits - add money on account
curl -X PUT  "localhost:4567/accounts/1/deposits?amount=10.45" | json_pp
{
   "id" : 1,
   "amount" : 20.573
}

PUT accounts/:accountId/withdrawals - withdraw money from account
curl -X PUT  "localhost:4567/accounts/1/withdrawals?amount=-10" | json_pp
{
   "amount" : 10.573,
   "id" : 1
}
```

Transaction api
```
POST /accounts/transactions - make transaction between accounts
curl -X POST --data '{"fromId":1, "toId":2, "amount":4}' "localhost:4567/accounts/transactions" | json_pp
{
   "fromId" : 1,
   "toId" : 2,
   "amount" : 4
}
```

Realization details

0. **Java 10** as programming language
1. **Gradle 4.10** as build automation system
2. **Spark** as http-framework
3. **Guice** as IoC container
4. **H2** as in memory database
5. **Gson** as json converter
6. **Junit5** as testing framework

Run unit tests
```
./gradlew :account-api:clean :account-api:test
```

Build executable jar
```
./gradlew :account-api:fatJar
```

Local run
```
nohup java -jar account-api-all-1.0-SNAPSHOT.jar &
```

Stop application
```
pkill -f account-api-all
```
