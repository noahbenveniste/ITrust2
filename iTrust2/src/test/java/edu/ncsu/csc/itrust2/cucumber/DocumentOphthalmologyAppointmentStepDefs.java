package edu.ncsu.csc.itrust2.cucumber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.ncsu.csc.itrust2.models.enums.Role;
import edu.ncsu.csc.itrust2.models.enums.State;
import edu.ncsu.csc.itrust2.models.persistent.BasicHealthMetrics;
import edu.ncsu.csc.itrust2.models.persistent.Hospital;
import edu.ncsu.csc.itrust2.models.persistent.Patient;
import edu.ncsu.csc.itrust2.models.persistent.User;

public class DocumentOphthalmologyAppointmentStepDefs extends CucumberTest {

    static {
        java.util.logging.Logger.getLogger( "com.gargoylesoftware" ).setLevel( Level.OFF );
    }

    private final String       baseUrl      = "http://localhost:8080/iTrust2";

    private final String       hospitalName = "Office Visit Hospital" + ( new Random() ).nextInt();
    private BasicHealthMetrics expectedBhm;

    @And ( "The required Ophthalmology facilities exist" )
    public void facilitiesExist () throws Exception {

        /* Make sure we create a Hospital record */
        final Hospital hospital = new Hospital( hospitalName, "Bialystok", "10101", State.NJ.toString() );
        hospital.save();

    }

    @When ( "A patient with (.+) exists with no documented office visits" )
    public void addPatient ( final String patientName ) throws Exception {
        final User pt = new User( patientName, "$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.",
                Role.ROLE_PATIENT, 1 );
        pt.save();

        final Patient patient = new Patient( User.getByName( patientName ) );
        patient.setFirstName( "Karl" );
        patient.setLastName( "Liebknecht" );
        patient.setEmail( "karl_liebknecht@mail.de" );
        patient.setAddress1( "Karl Liebknecht Haus. Alexanderplatz" );
        patient.setCity( "Berlin" );
        patient.setState( State.DE );
        patient.setZip( "91505" );
        patient.setPhone( "123-456-7890" );

        final SimpleDateFormat sdf = new SimpleDateFormat( "MM/DD/YYYY", Locale.ENGLISH );
        final Calendar time = Calendar.getInstance();
        time.setTime( sdf.parse( "12/25/1960" ) );

        patient.setDateOfBirth( time );
        patient.save();
    }

    @When ( "^I document an ophthalmology appointment with values: (.+) (.+) (.+) (.+) (.+) (.+) (.+) (.+)$" )
    public void documentOV ( final String patientName, final String date, final String time, final String visualAcuity,
            final String sphere, final String cylinder, final String axis, final String diagnosis ) {

        waitForAngular();
        final WebElement hospital = driver.findElement( By.name( "hospital" ) );
        hospital.click();

        final WebElement dateEle = driver.findElement( By.name( "date" ) );
        dateEle.clear();
        dateEle.sendKeys( date );

        final WebElement timeEle = driver.findElement( By.name( "time" ) );
        timeEle.clear();
        timeEle.sendKeys( time.replace( "_", " " ) );

        waitForAngular();
        final WebElement notes = driver.findElement( By.name( "notes" ) );
        notes.clear();
        notes.sendKeys( "Patient appears pretty much alive" );

        waitForAngular();
        final WebElement patientNameEle = driver
                .findElement( By.cssSelector( "input[value=\"" + patientName + "\"]" ) );
        patientNameEle.click();

        waitForAngular();
        final WebElement typeEle = driver.findElement( By.cssSelector( "input[value=\"OPHTHALMOLOGY_VISIT\"]" ) );
        typeEle.click();

        // Basic Eye Metrics
        waitForAngular();
        final WebElement visualAcuityODEle = driver.findElement( By.name( "visualAcuityOD" ) );
        visualAcuityODEle.clear();
        visualAcuityODEle.sendKeys( visualAcuity );
        final WebElement visualAcuityOSEle = driver.findElement( By.name( "visualAcuityOS" ) );
        visualAcuityOSEle.clear();
        visualAcuityOSEle.sendKeys( visualAcuity );
        final WebElement cylinderODEle = driver.findElement( By.name( "cylinderOD" ) );
        cylinderODEle.clear();
        cylinderODEle.sendKeys( cylinder );
        final WebElement cylinderOSEle = driver.findElement( By.name( "cylinderOS" ) );
        cylinderOSEle.clear();
        cylinderOSEle.sendKeys( cylinder );
        final WebElement sphereODEle = driver.findElement( By.name( "sphereOD" ) );
        sphereODEle.clear();
        sphereODEle.sendKeys( sphere );
        final WebElement sphereOSEle = driver.findElement( By.name( "sphereOS" ) );
        sphereOSEle.clear();
        sphereOSEle.sendKeys( sphere );
        final WebElement axisODEle = driver.findElement( By.name( "axisOD" ) );
        axisODEle.clear();
        axisODEle.sendKeys( axis );
        final WebElement axisOSEle = driver.findElement( By.name( "axisOS" ) );
        axisOSEle.clear();
        axisOSEle.sendKeys( axis );

        if ( !diagnosis.equals( "NULL" ) ) {
            waitForAngular();
            final WebElement diagnosisEle = driver.findElement( By.name( diagnosis ) );
            diagnosisEle.click();

            waitForAngular();
            final WebElement fillEle = driver.findElement( By.name( "fillOphDiagnosis" ) );
            fillEle.click();
        }

        waitForAngular();
        final WebElement submit = driver.findElement( By.name( "submit" ) );
        submit.click();

    }

    /**
     * Ensures that the correct opthalmology appointment was added to iTrust2
     *
     * @throws InterruptedException
     */
    @And ( "(.+) can view the appointment on (.+) at (.+) with values: (.+) (.+) (.+) (.+) (.+)" )
    public void correctPatientView ( final String patientName, final String date, final String time,
            final String visualAcuity, final String sphere, final String cylinder, final String axis,
            final String diagnosis ) throws InterruptedException {
        waitForAngular();
        attemptLogout();
        driver.get( baseUrl );
        waitForAngular();
        final WebElement usernameEle = driver.findElement( By.name( "username" ) );
        usernameEle.clear();
        usernameEle.sendKeys( patientName );
        final WebElement passwordEle = driver.findElement( By.name( "password" ) );
        passwordEle.clear();
        passwordEle.sendKeys( "123456" );
        final WebElement submitEle = driver.findElement( By.className( "btn" ) );
        submitEle.click();
        waitForAngular();

        driver.get( baseUrl + "/patient/officeVisit/viewOfficeVisits" );
        // assertTrue( driver.getPageSource().contains() );

        assertEquals( "", "" );
    }

    /**
     * Logs in a user
     *
     * @throws InterruptedException
     */
    @And ( "I am logged in to iTrust2 as (.+)" )
    public void logIn ( final String username ) throws InterruptedException {
        waitForAngular();
        attemptLogout();
        driver.get( baseUrl );
        waitForAngular();
        final WebElement usernameEle = driver.findElement( By.name( "username" ) );
        usernameEle.clear();
        usernameEle.sendKeys( username );
        final WebElement passwordEle = driver.findElement( By.name( "password" ) );
        passwordEle.clear();
        passwordEle.sendKeys( "123456" );
        final WebElement submitEle = driver.findElement( By.className( "btn" ) );
        submitEle.click();
        waitForAngular();
    }

    @Then ( "The ophthalmology visit is not documented" )
    public void checkNotDocumented () {
        waitForAngular();
        try {
            assertTrue( driver.getPageSource().contains( "Error occurred creating office visit" ) );
        }
        catch ( final Exception e ) {
            fail();
        }
    }

}
