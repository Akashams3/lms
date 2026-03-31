package com.project.lms.services;

import com.project.lms.entity.Packet;
import com.project.lms.repository.PacketRepository;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;

@Service
public class TinyGSMqttService {

    @Autowired
    private PacketRepository packetRepository;

    private static final String BROKER = "ssl://mqtt.tinygs.com:8883";
    private static final String CLIENT_ID = "ritstellar-client";

    private static final String USERNAME = "1478844343";
    private static final String PASSWORD = "b7U5VjsD!BTuDARR";

    @PostConstruct
    public void start() {
        connect();
    }

    public void connect() {
        try {

            MqttClient client = new MqttClient(BROKER, CLIENT_ID);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            options.setSocketFactory(sslContext.getSocketFactory());

            client.connect(options);

            System.out.println("Connected to TinyGS MQTT");

            client.subscribe("tinygs/+/packets");

            client.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message) {

                    String payload = new String(message.getPayload());

                    Packet packet = new Packet();
                    packet.setTopic(topic);
                    packet.setPayload(payload);
                    packet.setReceivedAt(LocalDateTime.now());

                    packetRepository.save(packet);

                    System.out.println("Packet stored: " + payload);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("MQTT connection lost");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}