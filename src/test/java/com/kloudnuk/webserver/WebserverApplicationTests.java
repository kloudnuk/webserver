package com.kloudnuk.webserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.HttpClientErrorException;

import com.kloudnuk.webserver.daos.api.IDeviceRepo;
import com.kloudnuk.webserver.daos.api.IOrgRepo;
import com.kloudnuk.webserver.daos.api.IUserAuthorityRepo;
import com.kloudnuk.webserver.daos.api.IUserRepo;
import com.kloudnuk.webserver.ddos.DeviceDdo;
import com.kloudnuk.webserver.ddos.UserAuthorityDdo;
import com.kloudnuk.webserver.ddos.UserDdo;
import com.kloudnuk.webserver.enums.DeviceStatus;
import com.kloudnuk.webserver.models.Device;
import com.kloudnuk.webserver.models.EntityUpdate;
import com.kloudnuk.webserver.models.Org;
import com.kloudnuk.webserver.models.SoftwarePackage;
import com.kloudnuk.webserver.models.User;
import com.kloudnuk.webserver.models.UserAuthority;
import com.kloudnuk.webserver.services.api.IDataStoreManager;
import com.kloudnuk.webserver.services.api.IDataStoreProvider;
import com.kloudnuk.webserver.services.api.IDistributionProvider;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.List;
import java.util.stream.Stream;
import java.util.NoSuchElementException;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kloudnuk Test Module
 * 
 * @author Victor Smolinski
 * @version 0.0.1
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebserverApplicationTests {
	final Logger log = LoggerFactory.getLogger(WebserverApplicationTests.class);

	@Autowired
	IDeviceRepo deviceRepo;

	@Autowired
	IOrgRepo orgRepo;

	@Autowired
	IUserRepo userRepo;

	@Autowired
	IUserAuthorityRepo userAuthorityRepo;

	@Autowired
	IDistributionProvider distributor;

	@Autowired
	IDataStoreProvider dsprovider;

	@Autowired
	IDataStoreManager dsmanager;

	RestTemplate rest = new RestTemplate();

	static String TEST_URI = "http://localhost:8000/api/v1";
	static String ADMIN_TESTUSR = "nukmaster";
	static String ADMIN_TESTORG = "vitekorp";
	static String ADMIN_TESTPASS = "linvic2024";

	@Test
	@Order(1)
	void contextLoads() {}

	/**
	 * DATA ACCESS OBJECT TEST UNITS
	 */

	@Test
	@Order(2)
	@DisplayName("Device Repo Create/Read-All Test")
	void DeviceCreateRead() {

		List<DeviceDdo> testdevices = new ArrayList<DeviceDdo>();

		testdevices.add(new DeviceDdo(UUID.fromString("72c00007-8c00-4708-0148-0944547420d3"),
				"test_nuk", "test unit case description", "192.168.1.254", "002:07:cf:16:1c:6b",
				DeviceStatus.INACTIVE, "192.168.1.1", "10.0.0.3", 2L));
		deviceRepo.insert(testdevices);

		List<DeviceDdo> assertDevices = deviceRepo.readAll().toList();
		assertEquals(
				testdevices.stream().filter(dev -> dev.name().equals("test_nuk"))
						.collect(Collectors.toList()).get(0).name(),
				assertDevices.stream().filter(dev -> dev.name().equals("test_nuk"))
						.collect(Collectors.toList()).get(0).name());
	}

	@Test
	@Order(3)
	@DisplayName("Device Repo Read-By-Id Test")
	void DeviceReadById() {
		Optional<DeviceDdo> dev =
				deviceRepo.readById(UUID.fromString("72c00007-8c00-4708-0148-0944547420d3"));
		assertEquals("test_nuk", dev.get().name());
	}

	@Test
	@Order(4)
	@DisplayName("Device Repo Read-By-Name Test")
	void DeviceReadByName() {
		Optional<DeviceDdo> dev = deviceRepo.readByName("test_nuk");
		assertEquals(dev.get().controllerid().toString(), "72c00007-8c00-4708-0148-0944547420d3");
		assertEquals(dev.get().name(), "test_nuk");
	}

	@Test
	@Order(5)
	@DisplayName("Device Repo UpdateOne Test")
	void DeviceUpdateOne() {
		String description = """
				This is an orange pi 3 lts test device.
				It is a work in progress.
				""";

		deviceRepo.updateOne("description", description, "name", "test_nuk");
		Optional<DeviceDdo> dev = deviceRepo.readByName("test_nuk");
		assertEquals(dev.get().description(), description);
	}

	@Test
	@Order(6)
	@DisplayName("Device Repo Read By Org Test")
	void DeviceReadByOrg() {
		Stream<DeviceDdo> ddoStream = deviceRepo.readByOrg(ADMIN_TESTORG);
		DeviceDdo deviceDdo =
				ddoStream.filter(ddo -> ddo.name().equals("test_nuk")).findFirst().orElseThrow();
		assertEquals("test_nuk", deviceDdo.name());
	}

	@Test
	@Order(7)
	@DisplayName("Device Repo Delete One Test")
	void DeviceDeleteOne() {
		deviceRepo.delete("name", "test_nuk");
		assertThrows(EmptyResultDataAccessException.class, () -> {
			deviceRepo.readByName("test_nuk");
		});
	}

	@Test
	@Order(8)
	@DisplayName("Org Repo Read All Test")
	void OrgReadAll() {
		Stream<Org> orgStream = orgRepo.readAll();
		assertTrue(orgStream.toList().size() > 0);
	}

	@Test
	@Order(9)
	@DisplayName("Org Repo Create Test")
	void OrgCreate() {
		List<Org> orgList = new ArrayList<Org>();
		orgList.add(new Org("test-org"));
		orgRepo.create(orgList);
		Stream<Org> orgStream = orgRepo.readAll();
		Org org = orgStream.filter(o -> o.name().equals("test-org")).findFirst().orElseThrow();
		assertEquals("test-org", org.name());
	}

	@Test
	@Order(10)
	@DisplayName("Org Repo Read-By-Id Test")
	void OrgReadById() {
		Org org = orgRepo.readById(2).orElseThrow();
		assertEquals(ADMIN_TESTORG, org.name());
	}

	@Test
	@Order(11)
	@DisplayName("Org Repo Get Org Id Test")
	void OrgGetId() {
		Long id = orgRepo.getOrgId(ADMIN_TESTORG);
		assertEquals(2, id);
	}

	@Test
	@Order(12)
	@DisplayName("Org Repo Update One Test")
	void OrgUpdateOne() {
		orgRepo.updateOne("name", "updated-test-org", "name", "test-org");
		Org org = orgRepo.readAll().filter(o -> o.name().equals("updated-test-org")).findFirst()
				.get();
		assertEquals("updated-test-org", org.name());
	}

	@Test
	@Order(13)
	@DisplayName("Org Repo Delete One Test")
	void OrgDeleteOne() {
		orgRepo.delete("name", "updated-test-org");

		orgRepo.delete("name", "updated-test-org");
		assertThrows(NoSuchElementException.class, () -> {
			orgRepo.readAll().filter(o -> o.name().equals("updated-test-org")).findFirst()
					.orElseThrow();
		});
	}

	@Test
	@Order(14)
	@DisplayName("User Repo Read All Test")
	void UserReadAll() {
		List<User> ddoList = userRepo.readAll(ADMIN_TESTORG).toList();
		assertTrue(ddoList.size() > 0);
	}

	@Test
	@Order(15)
	@DisplayName("User Repo Insert Test")
	void UserInsert() {
		List<UserDdo> ddoList = new ArrayList<UserDdo>();
		UserDdo ddo =
				new UserDdo("nuk_usertest", "autotest@noreply.com", 2L, "test-password", true);
		ddoList.add(ddo);
		userRepo.insert(ddoList, 2L);
		User retrieved = userRepo.readByName("nuk_usertest").orElseThrow();
		assertEquals(ddo.name(), retrieved.name());
	}

	@Test
	@Order(16)
	@DisplayName("User Repo Get and Read By Id Test")
	void UserGetReadId() {
		Long userid = userRepo.getUserId("nuk_usertest");
		User user = userRepo.readById(userid).orElseThrow();
		assertEquals("nuk_usertest", user.name());
	}

	@Test
	@Order(17)
	@DisplayName("User Repo Read By Name Test")
	void UserReadByName() {
		User user = userRepo.readByName("nuk_usertest").orElseThrow();
		assertEquals("nuk_usertest", user.name());
	}

	@Test
	@Order(18)
	@DisplayName("User Repo Update One")
	void UserUpdateOne() {
		userRepo.updateOne("name", "updated_usertest", "name", "nuk_usertest");
		assertEquals("updated_usertest",
				userRepo.readByName("updated_usertest").orElseThrow().name());
	}

	@Test
	@Order(19)
	@DisplayName("UserAuthority Insert and Read By Username Test")
	void UserAuthorityInsert() {
		Long userid = userRepo.getUserId("updated_usertest");
		UserAuthorityDdo ddo = new UserAuthorityDdo(userid, 3);
		List<UserAuthorityDdo> authList = new ArrayList<UserAuthorityDdo>();
		authList.add(ddo);
		userAuthorityRepo.insert(authList);
		UserAuthority testauth = userAuthorityRepo.readByUser("updated_usertest")
				.filter(auth -> auth.role().equals("CONTRIBUTOR")).findFirst().orElseThrow();
		assertEquals("CONTRIBUTOR", testauth.role());
	}

	@Test
	@Order(20)
	@DisplayName("UserAuthority Read By Role Test")
	void UserAuthorityReadByRole() {
		assertTrue(userAuthorityRepo.readByRole("CONTRIBUTOR").toList().size() > 0);
	}

	@Test
	@Order(21)
	@DisplayName("UserAuthority Read By Id Test")
	void UserAuthorityReadById() {
		UserAuthority userauth = userAuthorityRepo.readById(1L).orElseThrow();
		assertEquals(userauth.username(), "nukmaster");
	}

	@Test
	@Order(22)
	@DisplayName("UserAuthority Update One Test")
	void UserAuthorityUpdateOne() {
		userAuthorityRepo.updateOne("authorityid", Long.toString(2), "userid",
				Long.toString(userRepo.getUserId("updated_usertest")));
	}

	@Test
	@Order(23)
	@DisplayName("UserAUthority Delete One Test")
	void UserAUthorityDeleteOne() {
		Long userid = userRepo.getUserId("updated_usertest");
		userAuthorityRepo.delete("userid", Long.toString(userid));
		assertTrue(userAuthorityRepo.readByUser("updated_usertest").toList().size() == 0);
	}

	@Test
	@Order(24)
	@DisplayName("User Repo Delete One")
	void UserDeleteOne() {
		userRepo.delete("name", "updated_usertest");
		assertThrows(EmptyResultDataAccessException.class, () -> {
			userRepo.readByName("updated_usertest");
		});
	}

	/**
	 * INTEGRATED REST API TEST UNITS
	 */

	@SuppressWarnings("null")
	@Test
	@Order(25)
	@DisplayName("Enroll Device API Test")
	void EnrollDevicesAPI() {
		rest.getInterceptors()
				.add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));

		Device enrollee = new Device(UUID.fromString("5bc00007-8c00-4708-0148-0944547420d3"),
				"device_enroll_apitest", "api_test device", "192.168.1.222", "002:07:cf:16:ab:ba",
				DeviceStatus.RUNNING, "192.168.1.1", "10.0.0.222");
		Device[] inserts = {enrollee};

		var devices = rest.postForObject(TEST_URI.concat("/devices/enroll?org=" + ADMIN_TESTORG),
				inserts, Device[].class);

		for (var dev : devices) {
			if (dev.name().equals("device_enroll_apitest")) {
				assertEquals(enrollee.controllerid(), dev.controllerid());
			}
		}
	}

	@SuppressWarnings("null")
	@Test
	@Order(26)
	@DisplayName("Read All Devices API Test")
	void ReadAllDevicesAPI() {
		rest.getInterceptors()
				.add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));
		var devices = rest.getForObject(TEST_URI.concat("/devices/?org=" + ADMIN_TESTORG),
				Device[].class);
		assertTrue(devices.length >= 1);
	}

	@SuppressWarnings("null")
	@Test
	@Order(27)
	@DisplayName("Authorize Device API Test")
	void AuthorizeDeviceAPI() {
		rest.getInterceptors()
				.add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));

		UUID deviceid = UUID.fromString("5bc00007-8c00-4708-0148-0944547420d3");

		RequestEntity<Void> request = RequestEntity
				.post(TEST_URI.concat(
						"/devices/activate?softwarePackage=bacnet-client_0.0.15.zip&deviceid="
								+ deviceid.toString() + "&org=" + ADMIN_TESTORG))
				.accept(MediaType.ALL).build();

		var response = rest.exchange(request, Resource.class);

		assertTrue(response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200)));
		assertEquals("bacnet-client_0.0.15.zip", response.getBody().getFilename());
	}

	@Test
	@Order(28)
	@DisplayName("Update Device Info API Test")
	void UpdateDeviceAPI() {
		rest.getInterceptors()
				.add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));
		EntityUpdate updateobj = new EntityUpdate("name", "new device name via update api",
				"controllerid", "5bc00007-8c00-4708-0148-0944547420d3");
		String uri = TEST_URI
				.concat("/devices/edit/5bc00007-8c00-4708-0148-0944547420d3?org=" + ADMIN_TESTORG);
		RequestEntity<EntityUpdate> request =
				RequestEntity.put(uri).accept(MediaType.APPLICATION_JSON).body(updateobj);
		try {
			ResponseEntity<Void> response = rest.exchange(request, Void.class);
			assertTrue(response.getStatusCode().is2xxSuccessful());
		} catch (Exception e) {
			log.error("Update Device Info API Layer Error... ", e);
		}
	}

	@SuppressWarnings("null")
	@Test
	@Order(29)
	@DisplayName("Read All Users API Test")
	void ReadUsersAPI() {
		rest.getInterceptors()
				.add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));
		var users =
				rest.getForObject(TEST_URI.concat("/users/?org=" + ADMIN_TESTORG), User[].class);
		assertTrue(users.length > 0);
	}

	@SuppressWarnings("null")
	@Test
	@Order(30)
	@DisplayName("Create Users API Test")
	void CreateUsersAPI() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));

		// List<User> updateUsers = new ArrayList<User>();
		// updateUsers.add(new User("useradd-apitest", "useradd-apitest@nopreply.com",
		// ADMIN_TESTORG,
		// "apitest_userpass", true));

		// var currentUsers =
		// rest.getForObject(TEST_URI.concat("/users/?org=" + ADMIN_TESTORG), User[].class);

		// RequestEntity<List<User>> request = RequestEntity
		// .post(TEST_URI.concat("/users/create?org=" + ADMIN_TESTORG + "&genpass=false"))
		// .accept(MediaType.APPLICATION_JSON).body(updateUsers);
		// ResponseEntity<User[]> response = rest.exchange(request, User[].class);

		// log.info("CREATE USERS API TEST - Response Status: "
		// .concat(response.getStatusCode().toString()));
		// log.info("CREATE USERS API TEST - Existing user count: "
		// .concat(String.valueOf(currentUsers.length)));
		// log.info("CREATE USERS API TEST - New user count: "
		// .concat(String.valueOf(response.getBody().length)));

		// var newUsers =
		// rest.getForObject(TEST_URI.concat("/users/?org=" + ADMIN_TESTORG), User[].class);

		// assertTrue(response.getStatusCode().is2xxSuccessful());
		// assertTrue(response.getBody().length == 1);
		// assertTrue(currentUsers.length < newUsers.length);
	}

	@SuppressWarnings("null")
	@Test
	@Order(31)
	@DisplayName("Authorize User API Test")
	void AuthorizeUserApi() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));

		// var userauth = new UserAuthority("useradd-apitest", "CONTRIBUTOR");
		// RequestEntity<UserAuthority> request =
		// RequestEntity.post(TEST_URI.concat("/users/authorize?org=" + ADMIN_TESTORG))
		// .accept(MediaType.APPLICATION_JSON).body(userauth);
		// ResponseEntity<Void> response = rest.exchange(request, Void.class);

		// assertTrue(response.getStatusCode().is2xxSuccessful());

	}

	@SuppressWarnings("null")
	@Test
	@Order(32)
	@DisplayName("Test User Authorization API (Positive)")
	void PositiveAuthorizedUserTest() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor("useradd-apitest", "apitest_userpass"));
		// var devices = rest.getForObject(TEST_URI.concat("/devices/?org=" + ADMIN_TESTORG),
		// Device[].class);
		// assertTrue(devices.length >= 1);
	}

	@Test
	@Order(33)
	@DisplayName("Delete Device API Test")
	void DeleteDeviceAPI() {
		rest.getInterceptors()
				.add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));
		rest.delete(
				TEST_URI.concat("/devices/remove?deviceid=5bc00007-8c00-4708-0148-0944547420d3&org="
						+ ADMIN_TESTORG));
	}

	@SuppressWarnings("null")
	@Test
	@Order(34)
	@DisplayName("Test User Authorization (Negative)")
	void NegativeAuthorizedUserTest() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor("useradd-apitest", "apitest_userpass"));

		// assertThrows(HttpClientErrorException.class, () -> {
		// rest.getForObject(TEST_URI.concat("/users/?org=" + ADMIN_TESTORG), User[].class);
		// });
	}

	@SuppressWarnings("null")
	@Test
	@Order(35)
	@DisplayName("Search User API Test")
	void UserSearchAPI() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));
		// RequestEntity<String> request =
		// RequestEntity.post(TEST_URI.concat("/users/search?org=vitekorp"))
		// .accept(MediaType.APPLICATION_JSON).body("useradd-apitest");
		// ResponseEntity<User> response = rest.exchange(request, User.class);

		// log.info("User Search API Test: ".concat(response.getBody().toString()));
		// assertTrue(response.getBody().email().equals("useradd-apitest@nopreply.com"));
	}

	@Test
	@Order(36)
	@DisplayName("Edit User API Test")
	void UserEditAPI() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));

		// EntityUpdate update = new EntityUpdate("email", "userupdate-apitest@nopreply.com",
		// "name",
		// "useradd-apitest");
		// RequestEntity<EntityUpdate> request =
		// RequestEntity.put(TEST_URI.concat("/users/edit?org=" + ADMIN_TESTORG)).body(update);
		// ResponseEntity<Void> response = rest.exchange(request, Void.class);
		// assertTrue(response.getStatusCode().is2xxSuccessful());
	}

	@Test
	@Order(37)
	@DisplayName("Suspend User API Test")
	void UserSuspendAPI() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));

		// RequestEntity<String> request =
		// RequestEntity.post(TEST_URI.concat("/users/suspend?org=" + ADMIN_TESTORG))
		// .body("useradd-apitest");
		// ResponseEntity<Void> response = rest.exchange(request, Void.class);
		// assertTrue(response.getStatusCode().is2xxSuccessful());
	}

	@Test
	@Order(38)
	@DisplayName("Change Password User API Test")
	void UserPasswordChangeAPI() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));
		// EntityUpdate passUpdate =
		// new EntityUpdate("useradd-apitest", "New updated password 1123", "", "");
		// RequestEntity<EntityUpdate> request = RequestEntity
		// .post(TEST_URI.concat("/users/changepass?org=" + ADMIN_TESTORG)).body(passUpdate);
		// ResponseEntity<Void> response = rest.exchange(request, Void.class);
		// assertTrue(response.getStatusCode().is2xxSuccessful());
	}

	@Test
	@Order(39)
	@DisplayName("Remove User API Test")
	void RemoveUserAPI() {
		// rest.getInterceptors()
		// .add(new BasicAuthenticationInterceptor(ADMIN_TESTUSR, ADMIN_TESTPASS));

		// List<RequestEntity<Void>> requests = new ArrayList<RequestEntity<Void>>();

		// requests.add(RequestEntity
		// .delete(TEST_URI.concat("/users/remove/useradd-apitest?org=" + ADMIN_TESTORG))
		// .build());
		// requests.add(RequestEntity
		// .delete(TEST_URI.concat("/users/remove/user-randpass-apitest?org=" + ADMIN_TESTORG))
		// .build());
		// for (RequestEntity<Void> request : requests) {
		// ResponseEntity<Void> response = rest.exchange(request, Void.class);
		// log.info("Delete response: " + response.getStatusCode().toString());
		// assertTrue(response.getStatusCode().is2xxSuccessful());
		// }
	}

	@Test
	@Order(40)
	@DisplayName("Create Organization API Test")
	void CreateOrgAPI() {
		User admin = new User("apitest_adminuser", "adminuser@apitest.com", "apitest-org",
				"apitest-adminpassword", true);
		RequestEntity<User> request =
				RequestEntity.post(TEST_URI.concat("/orgs/create")).body(admin);
		ResponseEntity<Void> response = rest.exchange(request, Void.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());

		// Clean up.
		userAuthorityRepo.delete("userid", String.valueOf(userRepo.getUserId("apitest_adminuser")));
		userRepo.delete("name", "apitest_adminuser");
		orgRepo.delete("name", "apitest-org");
	}

	@Test
	@Order(41)
	@DisplayName("List Software Packages Service Test")
	void ListSoftwarePackages() {
		List<SoftwarePackage> packages = distributor.listPackages();
		log.info("Software packages available: ");
		for (SoftwarePackage softwarePackage : packages) {
			log.info("\t".concat("name: " + softwarePackage.name()));
			log.info("\t".concat("version: " + softwarePackage.version()));
			log.info("\t".concat("release-date: " + softwarePackage.releaseDate()));
		}
	}

	@Test
	@Order(42)
	@DisplayName("Simple MongoDb Provider - Get All Devices with all properties")
	void MongoDbSimpleGetDevices() {
		MongoDatabase db = dsprovider.getDatabase("Playground");
		MongoCollection<Document> devices = dsprovider.getCollection(db, "Devices");
		List<Document> _devices = dsprovider.getDocumentsAsJson(devices, 0).join();
		List<Document> _2devices = dsprovider.getDocumentsAsJson(devices, 2).join();
		log.info("_devices count: " + String.valueOf(_devices.size()));
		log.info("_2devices count: " + String.valueOf(_2devices.size()));
	}

	@Test
	@Order(43)
	@DisplayName("Simple MongoDb Provider - Get All Devices with most important properties")
	void MongoDbSimpleGetExclusiveDevices() {
		List<Document> devices = dsprovider.getDeviceData("Playground",
				new String[] {"properties.device-name.value", "id", "address", "last synced"})
				.join();
		devices.forEach(dev -> log.info(dev.toJson()));
		log.info("device count: " + String.valueOf(devices.size()));
	}

	@Test
	@Order(44)
	@DisplayName("Simple MongoDb Provider - Get All Points")
	void MongoDbSimpleGetAllPoints() {
		List<Document> points = dsprovider.getPointLists("Playground").join();

		log.info("points count: " + String.valueOf(points.size()));
	}

	@Test
	@Order(45)
	@DisplayName("Simple MongoDb Provider - Get All Device Configuration Profiles")
	void MongoDbSimpleGetAllDeviceConfigs() {
		List<Document> configs = dsprovider.getDeviceConfigurations("Playground").join();
		log.info("device configuration count: " + String.valueOf(configs.size()));
	}

	@Test
	@Order(46)
	@DisplayName("Simple MongoDb Provider - Get Logs by timestamp")
	void MongoDbSimpleGetLogs() {
		List<Document> logs = dsprovider.getDeviceLogs("Playground").join();
		log.info("Device Log Count: " + logs.size());
	}

	/**
	 * - All the logs for the day of July 29th 2024: {"timestamp": {$regex:
	 * /2024-07-29T\d{2}:\d{2}:\d{2}\+\d{4}/}} - All the logs that happened in one minute:
	 * {"timestamp": {$regex: /2024-07-29T16:33:\d{2}\+\d{4}/}} - All the logs from 6PM to 8PM
	 * inclusive: {"timestamp": {$regex: /2024-07-29T1[8-9]|2[0-2]:\d{2}:\d{2}\+\d{4}/}}
	 */
	@Test
	@Order(47)
	@DisplayName("Simple MongoDb Provider - Get Device Log by id")
	void MongoDbSimpleGetLogById() {
		try {
			List<Document> logs =
					dsprovider
							.getDeviceLogsByRegex("Playground",
									"2024-07-0[0-9]|1[0-9]|2[0-9]|T\\d{2}:\\d{2}:\\d{2}\\+\\d{4}")
							.join();
			log.info("Device Log Count: " + logs.size());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Test
	@Order(48)
	@DisplayName("MongoDb Connections Manager - Create New DB User")
	void MongoDbManagerCreateNewDbUser() {
		dsmanager.createDbUser(ADMIN_TESTORG, "Test_Device_UUID");
	}

	@Test
	@Order(49)
	@DisplayName("MongoDb Connections Manager - Create New DB User")
	void MongoDbManagerCreateX509Cert() {
		dsmanager.createX509Cert("Test_Device_UUID", 3);
	}
}
