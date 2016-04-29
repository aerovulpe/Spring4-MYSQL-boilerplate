package com.namespace.model;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Aaron on 23/04/2016.
 */
@Entity
@Table(name = "ipTable")
public class IpAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NaturalId
    private String ipAddress;
    private boolean banned;
    private int timesSeen;
    private Timestamp firstSeen;
    private Timestamp lastSeen;

    public IpAddress() {
    }

    public IpAddress(String ipAddress, boolean banned) {
        this.ipAddress = ipAddress;
        this.banned = banned;
        Timestamp firstSeen = new Timestamp(System.currentTimeMillis());
        this.firstSeen = firstSeen;
        this.lastSeen = firstSeen;
        incrementTimesSeen();
    }

    public IpAddress(String ipAddress) {
        this(ipAddress, false);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public int getTimesSeen() {
        return timesSeen;
    }

    public void setTimesSeen(int timesSeen) {
        this.timesSeen = timesSeen;
    }

    public Timestamp getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(Timestamp firstSeen) {
        this.firstSeen = firstSeen;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void incrementTimesSeen() {
        timesSeen++;
    }

    @Override
    public String toString() {
        return "IpAddress{" +
                "id=" + id +
                ", ipAddress='" + ipAddress + '\'' +
                ", banned=" + banned +
                ", timesSeen=" + timesSeen +
                ", firstSeen=" + firstSeen +
                ", lastSeen=" + lastSeen +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IpAddress ipAddress1 = (IpAddress) o;

        if (isBanned() != ipAddress1.isBanned()) return false;
        if (getTimesSeen() != ipAddress1.getTimesSeen()) return false;
        if (!getId().equals(ipAddress1.getId())) return false;
        if (!getIpAddress().equals(ipAddress1.getIpAddress())) return false;
        if (!getFirstSeen().equals(ipAddress1.getFirstSeen())) return false;
        return getLastSeen().equals(ipAddress1.getLastSeen());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getIpAddress().hashCode();
        result = 31 * result + (isBanned() ? 1 : 0);
        result = 31 * result + getTimesSeen();
        result = 31 * result + getFirstSeen().hashCode();
        result = 31 * result + getLastSeen().hashCode();
        return result;
    }
}
