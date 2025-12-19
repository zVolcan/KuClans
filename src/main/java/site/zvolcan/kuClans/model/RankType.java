package site.zvolcan.kuClans.model;

public enum RankType {
    LEADER("clan.leader-rank-name", "Leader"),
    MOD("clan.leader-rank-name", "Mod"),
    MEMBER("clan.leader-rank-name", "Member");

    private final String name;
    private final String defaultName;

    RankType(String name, String defaultName) {
        this.name = name;
        this.defaultName = defaultName;
    }

    public String getName() {
        return name;
    }

    public String getDefaultName() {
        return defaultName;
    }
}
