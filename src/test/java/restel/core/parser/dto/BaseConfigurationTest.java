package restel.core.parser.dto;

import com.techconative.restel.core.parser.dto.BaseConfig;
import org.junit.Assert;
import org.junit.Test;

public class BaseConfigurationTest {
    @Test
    public void TestBaseConfig() {
        BaseConfig baseConfig = new BaseConfig();

        baseConfig.setAppName("appName");
        Assert.assertEquals("appName", baseConfig.getAppName());

        baseConfig.setBaseUrl("Url");
        Assert.assertEquals("Url", baseConfig.getBaseUrl());

        baseConfig.setDefaultHeader("header");
        Assert.assertEquals("header", baseConfig.getDefaultHeader());

        Assert.assertNotEquals(new BaseConfig(), baseConfig);
        Assert.assertNotEquals(baseConfig.hashCode(), new BaseConfig().hashCode());
        Assert.assertNotNull(baseConfig.toString());

    }
}
