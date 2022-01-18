package dtu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import dtu.ReportService.Application.ReportService;
import dtu.ReportService.Domain.Payment;
import dtu.ReportService.Domain.PaymentMerchant;
import dtu.ReportService.Infrastructure.ReportRepository;
import dtu.ReportService.Presentation.ReportEventHandler;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.EventResponse;
import messaging.implementations.MockMessageQueue;

public class ReportRequestsSteps {
	ReportService reportService = new ReportService(new ReportRepository());
	private static MockMessageQueue messageQueue = new MockMessageQueue();
	private ReportEventHandler reportEventHandler = new ReportEventHandler(messageQueue, reportService);

	String customer1, customer2, customer3, merchant1, merchant2, merchant3, token1, token2, token3, amount1, amount2, amount3;
	HashSet<Payment> report;
	HashSet<PaymentMerchant> merchantReport;
	String sessionId, userId;
	
	@Given("a custom report exists")
	public void aReportExists() {
		customer1 = "customerId1";
		customer2 = "customerId2";
		customer3 = "customerId3";
		merchant1 = "merchantId1";
		merchant2 = "merchantId2";
		merchant3 = "merchantId3";
		token1 = "uniqueTokenId1";
		token2 = "uniqueTokenId2";
		token3 = "uniqueTokenId3";
		amount1 = "300";
		amount2 = "200";
		amount3 = "50";
		Payment payment1 = new Payment(customer1, merchant1, token1, amount1);
		Payment payment2 = new Payment(customer1, merchant2, token2, amount2);
		Payment payment3 = new Payment(customer2, merchant1, token3, amount3);
		reportService.put(payment1);
		reportService.put(payment2);
		reportService.put(payment3);
	}
	
	@Given("a user with id {string} has a payment in the report")
	public void aUserWithIdHasAReport(String userId) {
		this.userId = userId;
	}
	
	// Customer report request scenario
	@When("the customer requests his report with session id {string}")
	public void theCustomerRequestsHisReportWithSessionId(String sessionId) {
		this.sessionId = sessionId;
		EventResponse eventResponse = new EventResponse(sessionId, true, null, userId);
		Event reportRequestEvent = new Event("CustomerReportRequest", new Object[] {eventResponse});
		reportEventHandler.handleCustomerReportRequest(reportRequestEvent);
	}
	
	@Then("the customer report is put on the messagequeue")
	public void theCustomerReportIsPutOnTheMessagequeue() {
		var report = reportService.getCustomerReport(userId);
		EventResponse eventResponse = new EventResponse(sessionId, true, null, report);
		Event expectedResponseEvent = new Event("CustomerReportResponse." + sessionId, new Object[] { eventResponse });
		Event actualResponseEvent = messageQueue.getEvent("CustomerReportResponse." + sessionId);
		assertEquals(expectedResponseEvent, actualResponseEvent);
	}
	
	
	// Merchant report request scenario
	@When("the merchant requests his report with session id {string}")
	public void theMerchantRequestsHisReportWithSessionId(String sessionId) {
		this.sessionId = sessionId;
		EventResponse eventResponse = new EventResponse(sessionId, true, null, userId);
		Event reportRequestEvent = new Event("MerchantReportRequest", new Object[] {eventResponse});
		reportEventHandler.handleMerchantReportRequest(reportRequestEvent);
	}

	@Then("the merchant report is put on the messagequeue")
	public void theMerchantReportIsPutOnTheMessagequeue() {
		var report = reportService.getMerchantReport(userId);
		EventResponse eventResponse = new EventResponse(sessionId, true, null, report);
		Event expectedResponseEvent = new Event("MerchantReportResponse." + sessionId, new Object[] { eventResponse });
		Event actualResponseEvent = messageQueue.getEvent("MerchantReportResponse." + sessionId);
		assertEquals(expectedResponseEvent, actualResponseEvent);
	}
	
	

	// Manager report request scenario
	@When("the manager requests his report with session id {string}")
	public void theManagerRequestsHisReportWithSessionId(String sessionId) {
		this.sessionId = sessionId;
		EventResponse eventResponse = new EventResponse(sessionId, true, null);
		Event reportRequestEvent = new Event("ManagerReportRequest", new Object[] {eventResponse});
		reportEventHandler.handleManagerReportRequest(reportRequestEvent);
	}

	@Then("the manager report is put on the messagequeue")
	public void theManagerReportIsPutOnTheMessagequeue() {
		var report = reportService.getManagerReport();
		EventResponse eventResponse = new EventResponse(sessionId, true, null, report);
		Event expectedResponseEvent = new Event("ManagerReportResponse." + sessionId, new Object[] { eventResponse });
		Event actualResponseEvent = messageQueue.getEvent("ManagerReportResponse." + sessionId);
		assertEquals(expectedResponseEvent, actualResponseEvent);
	}

}
