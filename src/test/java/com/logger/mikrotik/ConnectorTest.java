package com.logger.mikrotik;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

import javax.net.SocketFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectorTest {

    private static final String WRONG_HOST = "192.168.0.1";
    private static final int WRONG_PORT = 3000;
    private ApiConnection connection;

    @Test
    void testWithCorrectUserAndPass() throws MikrotikApiException {
        connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, ApiConnection.DEFAULT_PORT, 2000);
        connection.login(Config.USERNAME, Config.PASSWORD);
    }

    @Test
    void testWithEmptyUser() {
        Exception e = assertThrows(ApiConnectionException.class, () -> {
            connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, ApiConnection.DEFAULT_PORT, 2000);
            connection.login("", Config.PASSWORD);
        });
        Assert.assertTrue(e.getMessage().contains("API username cannot be empty"));
    }

    @Test
    void testWithIncorrectUser() {
        Exception e = assertThrows(MikrotikApiException.class, () -> {
            connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, ApiConnection.DEFAULT_PORT, 2000);
            connection.login("a", Config.PASSWORD);
        });
        Assert.assertTrue(e.getMessage().contains("invalid user name or password (6)"));
    }

    @Test
    void testWithIncorrectPassword() {
        Exception e = assertThrows(MikrotikApiException.class, () -> {
            connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, ApiConnection.DEFAULT_PORT, 2000);
            connection.login("a", Config.PASSWORD);
        });
        Assert.assertTrue(e.getMessage().contains("invalid user name or password (6)"));
    }

    @Test
    void testWithCorrectHost() throws MikrotikApiException {
        connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, ApiConnection.DEFAULT_PORT, 2000);
    }

//    @Test
//    void testWithIncorrectHost() {
//        Exception e = assertThrows(MikrotikApiException.class, () -> {
//            connection = ApiConnection.connect(SocketFactory.getDefault(), WRONG_HOST, ApiConnection.DEFAULT_PORT, 2000);
//        });
//        Assert.assertTrue(e.getMessage().contains("Error connecting to 192.168.0.1:8728 : Connect timed out: /192.168.0.1:8728"));
//    }

    @Test
    void testWithCorrectPort() throws MikrotikApiException {
        connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, ApiConnection.DEFAULT_PORT, 2000);
    }

//    @Test
//    void testWithIncorrectPort() {
//        Exception e = assertThrows(MikrotikApiException.class, () -> {
//            connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, WRONG_PORT, 2000);
//        });
//        Assert.assertTrue(e.getMessage().contains("Error connecting to 192.168.31.59:3000 : Connect timed out: /192.168.31.59:3000"));
//    }
}