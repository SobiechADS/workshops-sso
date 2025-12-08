# 1. Scenariusz: IdP-SAML / Id-Broker OIDC 


- k1: oidc.keycloak.local => identity broker
- k2: saml.keycloak.local => IdP


### Konfiguracja k1

- przechodzimy do realm `workshop-oidc-realm`
- wybieramy `Identity providers` -> `SAML`

W ramach konfiguracji identity providera wskazujemy dane jak poniżej:

 - entityId `http://oidc.keycloak.local:8180/realms/workshop-oidc-realm`
 > *Wartość `clientId` po stronie `keycloak-saml` musi zostać utworzone na podstawie wpisanego `entityId`*
 - alias `saml-idp`
 - SAML entity descriptor `http://saml.keycloak.local:8190/realms/workshop-saml-realm/protocol/saml/descriptor`
 - Want Assertions signed `On` 

<br>

### Konfiguracja k2

- przechodzimy do realm `workshop-saml-realm`
- wybieramy `Clients`

W ramach konfiguracji klienta wskazujemy dane jak poniżej:

- clientType `SAML`
- client Id `http://oidc.keycloak.local:8180/realms/workshop-oidc-realm`
- valid redirect URIs `http://oidc.keycloak.local:8180/*`
- post logout redirect URIs  `http://oidc.keycloak.local:8180/*`
- name `saml-idp / oidc-broker`
- Master SAML processing URL `http://oidc.keycloak.local:8180/realms/workshop-oidc-realm/broker/saml-idp/endpoint`

Zapisujemy, następnie konfigurujemy poniższe właściwości: 
- Sign assertions `On`
- SAML signature key `KEY_ID`
- User Metadata descriptor URL `http://oidc.keycloak.local:8180/realms/workshop-oidc-realm/protocol/saml/descriptor`


> Powyższą konfigurację testujemy poprzez przejście na pod adres: `http://localhost:9180/admin` i wybór `SAML-IDP`     

<br>

### Mapowanie

Dodajemy mapowanie roli:
- typ mappera `SAML Attribute to Role`

> Upewniamy się, czy w utworzonym kliencie mamy dołączone atrybuty np. możemy to sprawdzić weryfikując `SAMLResponse`.
W przeciwnym wypadku dodajemy obsługę roli w ramach domyślnego SCOPE wykorzystywanego do komunikacji między `brokerem` ,a `IdP`.

<br>

-------------------------------------------------------------------------------------

# 2. Scenariusz: IdP-OIDC / Id-Broker SAML

- k1: oidc.keycloak.local => IdP
- k2: saml.keycloak.local => Identity Broker

### Konfiguracja k1

- przechodzimy do realm `workshop-oidc-realm`
- wybieramy `Clients`


W ramach konfiguracji identity providera wskazujemy dane jak poniżej:

- clientType `OIDC`
- clientId `saml-oidc-app`
- valid redirect URIs `http://saml.keycloak.local:8190/realms/workshop-saml-realm/broker/oidc-idp/endpoint`
- post logout redirect URIs `http://saml.keycloak.local:8190/*`

<br>

Zapisujemy, następnie konfigurujemy poniższe właściwości:

- use PKCE `On`
- PKCE method `S256`
- backchannel logout `On`

>W ramach client-scopes dodajemy `realm-roles` aby zostały dodane w ramach `userInfo`

<br>

### Konfiguracja k2

- przechodzimy do realm `workshop-saml-realm`
- wybieramy `Identity providers` -> `OIDC Keycloak`

W ramach konfiguracji identity providera wskazujemy dane jak poniżej:

- discovery endpoint `http://oidc.keycloak.local:8180/realms/workshop-oidc-realm/.well-known/openid-configuration`
- alias `oidc-idp`
- client ID `saml-oidc-app`
-  > *`clientId` musi zostać utworzony po stronie `keycloak-oidc`*

Zapisujemy, następnie konfigurujemy poniższe właściwości:
- client authentication `JWT Signed with private key`
- client assertion sig algorithm `RS256`
- use PKCE `On`
- PKCE method `S256`
- validate Signatures `On`
- backchannel logout `On`


> Powyższą konfigurację testujemy poprzez przejście na pod adres: `http://localhost:9190/admin` i wybór `oidc-idp`

<br>

### Mapowanie

- typ mappera `Claim to Role`
- Claim `realm_access.roles`
- vaue `wprowadzamy analogicznie admin lub user`

> Dodajemy obsługę roli w ramach domyślnego SCOPE wykorzystywanego do komunikacji między `brokerem` ,a `IdP`.


[Powrót](README.md)

