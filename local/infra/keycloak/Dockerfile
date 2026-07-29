# https://www.keycloak.org/server/containers
FROM quay.io/keycloak/keycloak:26.0.4
ADD --chown=keycloak:keycloak --chmod=644 https://repo1.maven.org/maven2/ru/playa/keycloak/keycloak-kafka-provider/26.0.4/keycloak-kafka-provider-26.0.4.jar /opt/keycloak/providers/keycloak-kafka-provider-26.0.4.jar.jar
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]