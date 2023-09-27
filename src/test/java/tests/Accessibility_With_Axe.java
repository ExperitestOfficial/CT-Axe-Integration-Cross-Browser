package tests;

import com.deque.axe.AXE;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertTrue;

public class Accessibility_With_Axe {

    protected String ACCESS_KEY = "";
    protected RemoteWebDriver driver;
    protected DesiredCapabilities capabilities = new DesiredCapabilities();
    protected JavascriptExecutor jse;

    @BeforeMethod
    public void setUp(Method method) throws MalformedURLException {
        capabilities.setCapability("experitest:testName", method.getName());
        capabilities.setCapability("experitest:accessKey", ACCESS_KEY);
        capabilities.setCapability(CapabilityType.BROWSER_NAME, "chrome");
        capabilities.setCapability("experitest:osName", "Windows Server 2016");

        driver = new RemoteWebDriver(new URL("https://uscloud.experitest.com/wd/hub"), capabilities);
        jse = (JavascriptExecutor) driver;
    }

    @Test
    public void ada_with_axe_test(Method method) throws MalformedURLException {
        driver.get("https://www.digital.ai");

        JSONObject responseJson = new AXE.Builder(driver, new URL("https://axejsfile.s3.us-east-2.amazonaws.com/axe.min.js")).analyze();
        JSONArray violations = responseJson.getJSONArray("violations");

        if (violations.isEmpty()) {
            driver.executeScript("seetest:client.report", "No violations found", "true");
            System.out.println("No violation found!");
        } else {
            driver.executeScript("seetest:client.report", "Violations found", "false");

            int counter = 1;
            for (int i = 0; i < violations.length(); i++) {
                driver.executeScript("seetest:client.startStepsGroup", "Violation Group - " + counter++);

                JSONObject violation = violations.getJSONObject(i);

                String help = violation.getString("help");
                driver.executeScript("seetest:client.report", "Help: " + help, "false");

                JSONArray nodes = violation.getJSONArray("nodes");
                JSONObject node = nodes.getJSONObject(0);
                String failureSummary = node.getString("failureSummary");
                driver.executeScript("seetest:client.report", "Failure Summary: " + failureSummary, "false");

                driver.executeScript("seetest:client.stopStepsGroup");
            }
            AXE.writeResults(method.getName(), responseJson);
            assertTrue(false, AXE.report(violations));
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }

}
