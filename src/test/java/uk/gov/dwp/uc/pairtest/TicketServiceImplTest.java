package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TicketServiceImplTest {

  private TicketService ticketService;
  private TicketPaymentService paymentService;
  private SeatReservationService reservationService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup(){
    paymentService = Mockito.mock(TicketPaymentService.class);
    reservationService = Mockito.mock(SeatReservationService.class);
    ticketService = new TicketServiceImpl(paymentService, reservationService);
  }

  @Test
  public void testValidTicketsPurchase(){
    TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
    TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
    TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
    ticketService.purchaseTickets(1L, infant,child,adult);
    verify(paymentService,times(1)).makePayment(1L, 30);
    verify(reservationService,times(1)).reserveSeat(1L, 2);
  }

  @Test
  public void testOnlyInfantAndChildTicketsPurchase(){
    TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
    TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
    thrown.expect(InvalidPurchaseException.class);
    thrown.expectMessage("Child and infant tickets cannot be purchased without purchasing an Adult ticket");
    ticketService.purchaseTickets(1L, infant,child);
  }

  @Test
  public void testOnlyChildTicketsPurchase(){
    TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
    thrown.expect(InvalidPurchaseException.class);
    thrown.expectMessage("Child and infant tickets cannot be purchased without purchasing an Adult ticket");
    ticketService.purchaseTickets(1L, child);
  }

  @Test
  public void testOnlyInfantTicketsPurchase(){
    TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
    thrown.expect(InvalidPurchaseException.class);
    thrown.expectMessage("Child and infant tickets cannot be purchased without purchasing an Adult ticket");
    ticketService.purchaseTickets(1L, infant);
  }

  @Test
  public void testOnlyAdultTicketsPurchase(){
    TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
    ticketService.purchaseTickets(1L, adult);
    verify(paymentService,times(1)).makePayment(1L, 200);
    verify(reservationService,times(1)).reserveSeat(1L, 10);
  }

  @Test
  public void testMaxTicketsPurchase(){
    TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 10);
    TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10);
    TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 11);
    thrown.expect(InvalidPurchaseException.class);
    thrown.expectMessage("Cannot purchase more than 20 tickets at a time");
    ticketService.purchaseTickets(1L, infant,child,adult);
  }

  @Test
  public void testMaxTicketsIncludingInfantPurchase(){
    TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 10);
    TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
    TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 15);
    ticketService.purchaseTickets(1L, infant,child,adult);
    verify(paymentService,times(1)).makePayment(1L, 350);
    verify(reservationService,times(1)).reserveSeat(1L, 20);
  }

  @Test
  public void testNegativeTicketsPurchase(){
    thrown.expect(InvalidPurchaseException.class);
    thrown.expectMessage("Number of tickets cannot be less than 1");
    TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1);
    ticketService.purchaseTickets(1L, adult);
  }

  @Test
  public void testInvalidAccount(){
    TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
    thrown.expect(InvalidPurchaseException.class);
    thrown.expectMessage("Invalid account id");
    ticketService.purchaseTickets(-1L, adult);
  }

}
