package org.dreeam.leaf.version;

import org.galemc.gale.version.AbstractPaperVersionFetcher;

public class LeafVersionFetcher extends AbstractPaperVersionFetcher {

    public static final String DOWNLOAD_PAGE = "https://github.com/KiyroPrower/TintedPaper/releases";
    public static final String API_URL = "https://api.github.com/repos/KiyroPrower/TintedPaper";
    public static final String USER_AGENT = null;

    public LeafVersionFetcher() {
        super(
            DOWNLOAD_PAGE,
            "KiyroPrower",
            "TintedPaper",
            "KiyroPrower",
            "TintedPaper",
            API_URL,
            USER_AGENT,
            ApiType.GITHUB
        );
    }
}
