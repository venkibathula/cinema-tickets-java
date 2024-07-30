package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


public class TicketServiceImpl implements TicketService {
  /**
   * Should only have private methods other than the one below.
   */

  private TicketPaymentService paymentService;
  private SeatReservationService reservationService;

  private static final int CHILD_TICKET_PRICE = 10;
  private static final int ADULT_TICKET_PRICE = 20;
  private static final int MAX_TICKETS = 20;

  public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
    this.paymentService = paymentService;
    this.reservationService = reservationService;
  }

  @Override
  public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

    int totalTickets = 0;
    int adultTickets = 0;
    int totalAmount = 0;
    int infantTickets = 0;

    for (TicketTypeRequest request : ticketTypeRequests) {
      switch (request.getTicketType()) {
        case ADULT:
          totalTickets += request.getNoOfTickets();
          totalAmount += calculateAmount(request.getNoOfTickets(), ADULT_TICKET_PRICE);
          adultTickets += request.getNoOfTickets();
          break;
        case CHILD:
          totalTickets += request.getNoOfTickets();
          totalAmount += calculateAmount(request.getNoOfTickets(), CHILD_TICKET_PRICE);
          break;
        case INFANT:
          //Infants are free
          infantTickets += request.getNoOfTickets();
          break;
      }
    }
    //Validating business rules
    validateTicketPurchaseRules(totalTickets, adultTickets, infantTickets);
    validateAccount(accountId);

    paymentService.makePayment(accountId, totalAmount);
    reservationService.reserveSeat(accountId, totalTickets);
  }

  private int calculateAmount(int noOfTickets, int amount) {
    return noOfTickets * amount;
  }

  private void validateTicketPurchaseRules(int totalTickets, int adultTickets, int infantTickets) {
    if (totalTickets > MAX_TICKETS) {
      throw new InvalidPurchaseException("Cannot purchase more than " + MAX_TICKETS + " tickets at a time");
    }
    if ((totalTickets > 0 || infantTickets > 0) && adultTickets == 0) {
      throw new InvalidPurchaseException("Child and infant tickets cannot be purchased without purchasing an Adult ticket");
    }
  }

  private void validateAccount(long accountId) {
    if (accountId < 1) {
      throw new InvalidPurchaseException("Invalid account id");
    }
  }

}




