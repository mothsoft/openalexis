package com.mothsoft.integration.twitter;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class TwitterServiceImplTest {

    private TwitterServiceImpl service;
    private Properties properties;

    @Before
    public void setUp() {
        this.properties = new Properties();
        this.service = new TwitterServiceImpl(properties);
    }

    @Test
    public void testSearch() {
        assertNotNull(this.service.search("mothsoft"));
    }

    @Test
    public void testGetRequestToken() {
        assertNotNull(this.service.getRequestToken());
    }

}
