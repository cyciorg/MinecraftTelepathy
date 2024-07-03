package org.cyci.mc.minecrafttelepathy.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * org.cyci.mc.minecrafttelepathy.managers
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy.iml
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Wed - July/Wed/2024
 */
public class Party {

    private final UUID id;
    private final UUID leader;
    private final Set<UUID> members;

    public Party(UUID leader) {
        this.id = UUID.randomUUID();
        this.leader = leader;
        this.members = new HashSet<>();
        this.members.add(leader);
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID member) {
        members.add(member);
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public boolean isMember(UUID member) {
        return members.contains(member);
    }
}
