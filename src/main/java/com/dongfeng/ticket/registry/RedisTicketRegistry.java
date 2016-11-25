package com.dongfeng.ticket.registry;

import com.dongfeng.redis.RedisClient;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuchengdong@qbao.com on 2016/9/23.
 */
public final class RedisTicketRegistry extends AbstractDistributedTicketRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisTicketRegistry.class);

    private static final String[] BUSINESS_SPACE = new String[]{"TICKET"};

    /**
     * REDIS client.
     */
    @NotNull
    private final RedisClient client;

    /**
     * TGT cache entry timeout in seconds.
     */
    @Min(0)
    private final int tgtTimeout;

    /**
     * ST cache entry timeout in seconds.
     */
    @Min(0)
    private final int stTimeout;


    /**
     * This alternative constructor takes time in milliseconds.
     * It has the timeout parameters in order to create a unique method signature.
     *
     * @param ticketGrantingTicketTimeOut TGT timeout in milliseconds.
     * @param serviceTicketTimeOut        ST timeout in milliseconds.
     * @param client                      redisTemplate
     * @see com.dongfeng.redis.RedisClient
     * @deprecated This has been deprecated
     */
    @Deprecated
    public RedisTicketRegistry(final long ticketGrantingTicketTimeOut, final long serviceTicketTimeOut,
                               final RedisClient client) {
        this(client, (int) TimeUnit.MILLISECONDS.toSeconds(ticketGrantingTicketTimeOut),
                (int) TimeUnit.MILLISECONDS.toSeconds(serviceTicketTimeOut));
    }

    /**
     * Creates a new instance using the given redis client instance, which is presumably configured via.
     *
     * @param client                      Redis client.
     * @param ticketGrantingTicketTimeOut TGT timeout in seconds.
     * @param serviceTicketTimeOut        ST timeout in seconds.
     */
    public RedisTicketRegistry(final RedisClient client, final int ticketGrantingTicketTimeOut,
                               final int serviceTicketTimeOut) {
        this.tgtTimeout = ticketGrantingTicketTimeOut;
        this.stTimeout = serviceTicketTimeOut;
        this.client = client;

        LOGGER.debug("Construct RedisTicketRegistry instance,tgtTimeout={},stTimeout={}", tgtTimeout, stTimeout);
    }

    @Override
    protected void updateTicket(final Ticket ticket) {
        LOGGER.debug("Updating ticket {}", ticket);
        try {
            client.set(ticket.getId(), ticket, getTimeout(ticket), BUSINESS_SPACE);
        } catch (final Exception e) {
            LOGGER.error("Failed updating {}", ticket, e);
        }
    }

    @Override
    public void addTicket(final Ticket ticket) {
        LOGGER.debug("Adding ticket {}", ticket);
        try {
            client.set(ticket.getId(), ticket, getTimeout(ticket), BUSINESS_SPACE);
        } catch (final Exception e) {
            LOGGER.error("Failed adding {}", ticket, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteTicket(final String ticketId) {
        if (ticketId == null) {
            return false;
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return false;
        }

        if (ticket instanceof TicketGrantingTicket) {
            LOGGER.debug("Removing ticket children [{}] from the registry.", ticket);
            deleteChildren((TicketGrantingTicket) ticket);
        }

        LOGGER.debug("Deleting ticket {}", ticketId);
        try {
            client.del(ticketId, BUSINESS_SPACE);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Failed deleting {}", ticketId, e);
        }
        return false;
    }

    /**
     * Delete the TGT's service tickets.
     *
     * @param ticket the ticket
     */
    private void deleteChildren(final TicketGrantingTicket ticket) {
        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            for (final Map.Entry<String, Service> entry : services.entrySet()) {
                try {
                    client.del(entry.getKey(), BUSINESS_SPACE);
                    LOGGER.trace("Scheduled deletion of service ticket [{}]", entry.getKey());
                } catch (final Exception e) {
                    LOGGER.error("Failed deleting {}", entry.getKey(), e);
                }
            }
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        LOGGER.debug("getTicket {}", ticketId);

        try {
            final Ticket t = (Ticket) client.get(ticketId, BUSINESS_SPACE);
            if (t != null) {
                return getProxiedTicketInstance(t);
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching {} ", ticketId, e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * This operation is not supported.
     *
     * @throws UnsupportedOperationException if you try and call this operation.
     */
    @Override
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("GetTickets not supported.");
    }

    /**
     * @param sync set to true, if updates to registry are to be synchronized
     * @deprecated As of version 3.5, this operation has no effect since async writes can cause registry consistency issues.
     */
    @Deprecated
    public void setSynchronizeUpdatesToRegistry(final boolean sync) {
    }

    @Override
    protected boolean needsCallback() {
        return true;
    }

    /**
     * Gets the timeout value for the ticket.
     *
     * @param t the t
     * @return the timeout
     */
    private int getTimeout(final Ticket t) {
        if (t instanceof TicketGrantingTicket) {
            return this.tgtTimeout;
        } else if (t instanceof ServiceTicket) {
            return this.stTimeout;
        }
        throw new IllegalArgumentException("Invalid ticket type");
    }
}
