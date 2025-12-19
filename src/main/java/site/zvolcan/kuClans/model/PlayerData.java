package site.zvolcan.kuClans.model;

import javax.annotation.Nullable;
import java.util.UUID;

public final class PlayerData {

    private final UUID uuid;
    private Clan clan = null;
    private boolean clanChat = false;
    private String clanIdInvite = null;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Clan getClan() {
        return clan;
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

    public boolean isClanChat() {
        return clanChat;
    }

    public void setClanChat(boolean clanChat) {
        this.clanChat = clanChat;
    }

    @Nullable
    public String getClanIDInvite() {
        return clanIdInvite;
    }

    public void setClanIDInvite(String clanIdInvite) {
        this.clanIdInvite = clanIdInvite;
    }
}
