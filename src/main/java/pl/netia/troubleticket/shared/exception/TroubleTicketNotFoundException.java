package pl.netia.troubleticket.shared.exception;

public class TroubleTicketNotFoundException extends RuntimeException {

    public TroubleTicketNotFoundException(String id) {
        super("Trouble ticket not found: " + id);
    }
}
