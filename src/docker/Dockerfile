FROM ingrid-java
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-iplug-sns*-installer.jar /

RUN unzip ingrid-iplug-sns*-installer.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-iplug-sns \
    && cp -R /tmp/ingrid-iplug-*/* /opt/ingrid/ingrid-iplug-sns/ \
    && rm -Rf /tmp/* \
    && rm ingrid-*.jar

# COPY ./files /

RUN echo jetty.port=80 > /opt/ingrid/ingrid-iplug-sns/conf/config.override.properties
WORKDIR /opt/ingrid/ingrid-iplug-sns
EXPOSE 80

CMD /bin/sh start.sh start
