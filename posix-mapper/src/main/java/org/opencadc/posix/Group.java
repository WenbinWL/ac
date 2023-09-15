package org.opencadc.posix;

import jakarta.persistence.*;


@NamedQuery(name = "findGroupByName", query = "SELECT g FROM Groups g WHERE g.groupname = :groupname")
@Table(name = "Groups")
@Entity(name = "Groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "groups_gid_seq1")
    @SequenceGenerator(name = "groups_gid_seq1", sequenceName = "groups_gid_seq1", allocationSize = 1,
            initialValue = 10000)
    private int gid;

    private String groupname;

    public Group() {
    }

    public Group(String groupname) {
        this.groupname = groupname;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    @Override
    public String toString() {
        return "Group{" +
               "gid=" + gid +
               ", groupname='" + groupname + '\'' +
               '}';
    }
}