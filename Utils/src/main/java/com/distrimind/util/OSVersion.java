/*
Copyright or © or Copr. Jason Mahdjoub (04/02/2016)

jason.mahdjoub@distri-mind.fr

This software (Utils) is a computer program whose purpose is to give several kind of tools for developers
(ciphers, XML readers, decentralized id generators, etc.).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @author Jason Mahdjoub
 * @version 1.1
 * @since Utils 3.17
 */
public enum OSVersion {
    WINDOWS_3_11(".*(win16).*", OS.WINDOWS),
    WINDOWS_95(".*((windows.*95)|(win95)|(windows_95)).*", OS.WINDOWS, WINDOWS_3_11),
    WINDOWS_98(".*((windows.*98)|(win98)).*", OS.WINDOWS, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_NT_4_0(".*((windows.*nt.*4)|(winnt4.0)|(winnt)).*", OS.WINDOWS, WINDOWS_98, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_ME(".*((windows.*me)|(windows.*millennium)).*", OS.WINDOWS, WINDOWS_NT_4_0, WINDOWS_98, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_2000(".*((windows nt 5.0)|(windows.*2000)|(windows.*5.0)).*", OS.WINDOWS, WINDOWS_ME, WINDOWS_NT_4_0, WINDOWS_98, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_XP(".*((windows nt 5.1)|(windows.*xp)|(windows.*5.1)).*", OS.WINDOWS, WINDOWS_2000, WINDOWS_ME, WINDOWS_NT_4_0, WINDOWS_98, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_SERVER_2003(".*((windows nt 5.2)|(windows.*5.2)|(windows.*2003)).*", OS.WINDOWS, WINDOWS_XP, WINDOWS_2000, WINDOWS_ME, WINDOWS_NT_4_0, WINDOWS_98, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_VISTA(".*((windows nt 6.0)|(windows.*6.0)|(windows.*vista)).*", OS.WINDOWS, WINDOWS_SERVER_2003, WINDOWS_XP, WINDOWS_2000, WINDOWS_ME, WINDOWS_NT_4_0, WINDOWS_98, WINDOWS_95,WINDOWS_3_11),
    WINDOWS_7(".*((windows nt 6.1)|(windows.*7)|(windows.*6.1)).*", OS.WINDOWS, WINDOWS_VISTA, WINDOWS_SERVER_2003, WINDOWS_XP, WINDOWS_2000, WINDOWS_ME, WINDOWS_NT_4_0, WINDOWS_98, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_8(".*((windows nt 6.2)|(windows.*8)|(windows.*6.2)).*", OS.WINDOWS, WINDOWS_7, WINDOWS_VISTA, WINDOWS_SERVER_2003, WINDOWS_XP, WINDOWS_2000, WINDOWS_ME, WINDOWS_NT_4_0, WINDOWS_98, WINDOWS_95, WINDOWS_3_11),
    WINDOWS_10(".*((windows nt 10.0)|(windows.*10)).*", OS.WINDOWS, WINDOWS_8, WINDOWS_7, WINDOWS_VISTA, WINDOWS_SERVER_2003, WINDOWS_XP, WINDOWS_2000, WINDOWS_ME,WINDOWS_NT_4_0, WINDOWS_98,WINDOWS_95, WINDOWS_3_11),
    WINDOWS_UNKNOWN(".*((windows)|(win)).*", OS.WINDOWS),
    OPEN_BSD(".*openbsd.*", OS.OPEN_BSD),
    SUN_OS(".*(sunos).*", OS.SUN_OS),
    Ubuntu( ".*(ubuntu).*",OS.LINUX),
    LINUX( ".*((linux)|(x11)).*", OS.LINUX),
    IOS(".*((iphone)|(ipad)|(ios)).*", OS.IOS),
    MAC_OS_X_10_7( ".*((mac_powerPC)|(macintosh)|(mac)).*10.7.*",OS.MAC_OS_X),
    MAC_OS_X_10_8( ".*((mac_powerPC)|(macintosh)|(mac)).*10.8.*",OS.MAC_OS_X, MAC_OS_X_10_7),
    MAC_OS_X_10_9( ".*((mac_powerPC)|(macintosh)|(mac)).*10.9.*",OS.MAC_OS_X, MAC_OS_X_10_7, MAC_OS_X_10_8),
    MAC_OS_X_10_10( ".*((mac_powerPC)|(macintosh)|(mac)).*10.10.*",OS.MAC_OS_X, MAC_OS_X_10_7, MAC_OS_X_10_8, MAC_OS_X_10_9),
    MAC_OS_X_10_11( ".*((mac_powerPC)|(macintosh)|(mac)).*10.11.*",OS.MAC_OS_X, MAC_OS_X_10_7, MAC_OS_X_10_8, MAC_OS_X_10_9, MAC_OS_X_10_10),
    MAC_OS_X_10_12( ".*((mac_powerPC)|(macintosh)|(mac)).*10.12.*",OS.MAC_OS_X, MAC_OS_X_10_7, MAC_OS_X_10_8, MAC_OS_X_10_9, MAC_OS_X_10_10, MAC_OS_X_10_11),
    MAC_OS_X_10_13( ".*((mac_powerPC)|(macintosh)|(mac)).*10.13.*",OS.MAC_OS_X, MAC_OS_X_10_7, MAC_OS_X_10_8, MAC_OS_X_10_9, MAC_OS_X_10_10, MAC_OS_X_10_11, MAC_OS_X_10_12),
    MAC_OS_X_10_14( ".*((mac_powerPC)|(macintosh)|(mac)).*10.14.*",OS.MAC_OS_X, MAC_OS_X_10_7, MAC_OS_X_10_8, MAC_OS_X_10_9, MAC_OS_X_10_10, MAC_OS_X_10_11, MAC_OS_X_10_12, MAC_OS_X_10_13),
    MAC_OS_X_10_15( ".*((mac_powerPC)|(macintosh)|(mac)).*10.15.*",OS.MAC_OS_X, MAC_OS_X_10_7, MAC_OS_X_10_8, MAC_OS_X_10_9, MAC_OS_X_10_10, MAC_OS_X_10_11, MAC_OS_X_10_12, MAC_OS_X_10_13, MAC_OS_X_10_14),
    MAC_OS_X_UNKNOWN( ".*((mac_powerPC)|(macintosh)|(mac)).*",OS.MAC_OS_X),
    QNX(".*(qnx).*",OS.QNX),
    BeOS(".*(beos).*",OS.BEOS),
    OS_2(".*(os/2).*", OS.OS_2),
    ANDROID_1_BASE(".*(android).*",OS.ANDROID),
    ANDROID_2_BASE_1_1(".*(android).*",OS.ANDROID),
    ANDROID_3_CUPCAKE(".*(android).*",OS.ANDROID),
    ANDROID_4_DONUT(".*(android).*",OS.ANDROID),
    ANDROID_5_ECLAIR(".*(android).*",OS.ANDROID),
    ANDROID_6_ECLAIR_0_1(".*(android).*",OS.ANDROID),
    ANDROID_7_ECLAIR_MR1(".*(android).*",OS.ANDROID),
    ANDROID_8_FROYO(".*(android).*",OS.ANDROID),
    ANDROID_9_GINGERBREAD(".*(android).*",OS.ANDROID),
    ANDROID_10_GINGERBREAD_MR1(".*(android).*",OS.ANDROID),
    ANDROID_11_HONEYCOMB(".*(android).*",OS.ANDROID),
    ANDROID_12_HONEYCOMB_MR1(".*(android).*",OS.ANDROID),
    ANDROID_13_HONEYCOMB_MR2(".*(android).*",OS.ANDROID),
    ANDROID_14_ICE_SCREAM_SANDWICH(".*(android).*",OS.ANDROID),
    ANDROID_15_ICE_SCREAM_SANDWICH_MR1(".*(android).*",OS.ANDROID),
    ANDROID_16_JELLY_BEAN(".*(android).*",OS.ANDROID),
    ANDROID_17_JELLY_BEAN_MR1(".*(android).*",OS.ANDROID),
    ANDROID_18_JELLY_BEAN_MR2(".*(android).*",OS.ANDROID),
    ANDROID_19_KITKAT(".*(android).*",OS.ANDROID),
    ANDROID_20_KITKAT_WATCH(".*(android).*",OS.ANDROID),
    ANDROID_21_LOLLIPOP(".*(android).*",OS.ANDROID),
    ANDROID_22_LOLLIPOP_MR1(".*(android).*",OS.ANDROID),
    ANDROID_23_M(".*(android).*",OS.ANDROID),
    ANDROID_24_N(".*(android).*",OS.ANDROID),
    ANDROID_25_N_MR1(".*(android).*",OS.ANDROID),
    ANDROID_26_O(".*(android).*",OS.ANDROID),
    ANDROID_27_O_MR1(".*(android).*",OS.ANDROID),
    ANDROID_28_P(".*(android).*",OS.ANDROID),
    ANDROID_29_Q(".*(android).*",OS.ANDROID),
    ANDROID_UNKNOWN(".*(android).*",OS.ANDROID),
    SEARCH_BOT_NUHK(".*(nuhk).*",OS.SEARCH_BOT),
    SEARCH_BOT_GOOGLEBOT(".*(googlebot).*",OS.SEARCH_BOT),
    SEARCH_BOT_YAMMYBOT(".*(yammybot).*",OS.SEARCH_BOT),
    SEARCH_BOT_OPENBOT(".*(openbot).*",OS.SEARCH_BOT),
    SEARCH_BOT_SLURP(".*(slurp).*",OS.SEARCH_BOT),
    SEARCH_BOT_MSNBOT(".*(msnbot).*",OS.SEARCH_BOT),
    SEARCH_BOT_ASK_JEEVES_TEOMA(".*(ask jeeves/teoma).*",OS.SEARCH_BOT),
    SEARCH_BOT_ASK_QWANT(".*(qwant).*",OS.SEARCH_BOT),
    FREE_BSD_UNKNWON(".*(freebsd).*",OS.FREE_BSD);

    private final Pattern pattern;
    private final OS os;
    private final OSVersion[] compatibleVersions;

    OSVersion(String pattern, OS os, OSVersion... compatibleVersions) {
        this.pattern = Pattern.compile(pattern);
        this.os = os;
        this.compatibleVersions = compatibleVersions;
    }

    public static OSVersion getFrom(String userAgent) {
        for (OSVersion version : OSVersion.values()) {
            if (version.pattern.matcher(userAgent.toLowerCase()).matches())
                return version;
        }
        return null;
    }

    public OS getOS() {
        return os;
    }

    @SuppressWarnings("unused")
    public OSVersion[] getCompatibleVersions() {
        return compatibleVersions;
    }

    private static int getAndroidVersionInt()
    {
        try {
            Class<?> versionClass=Class.forName("android.os.Build$VERSION");
            return (int)versionClass.getDeclaredField("SDK_INT").get(null);
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            return 0;
        }
    }

    /*private static String getAndroidVersion()
    {
        try {
            Class<?> versionClass=Class.forName("android.os.Build.VERSION");
            return (String)versionClass.getDeclaredField("RELEASE").get(null);
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            return null;
        }
    }*/

    static private final OSVersion currentOS;
    static final String OS_VERSION = (System.getProperty("os.name") + " " + System.getProperty("os.version")).toLowerCase();
    static
    {
        OSVersion v=null;
        if (OS.isAndroid()) {
            for (OSVersion osv:OSVersion.values())
            {
                if (osv.getOS()==OS.ANDROID && osv.name().contains("_"+getAndroidVersionInt()+"_"))
                {
                    v=osv;
                }
            }
            if (v==null)
                v=ANDROID_UNKNOWN;
        }
        else {
            for (OS os : OS.values()) {
                if (os.pattern.matcher(OS.OSName).matches()) {
                    v=OSVersion.getFrom(OS_VERSION);
                    break;
                }
            }
        }
        currentOS=v;

    }

    public static OSVersion getCurrentOSVersion()
    {
        return currentOS;
    }

    @SuppressWarnings("unused")
    public List<OSVersion> getLowerVersions()
    {
        List<OSVersion> res=new ArrayList<>();
        for (OSVersion v : OSVersion.values())
            if (v.getOS()==this.getOS())
                if (v.ordinal()<this.ordinal())
                    res.add(v);
        return res;
    }

    @SuppressWarnings("unused")
    public List<OSVersion> getLowerOrEqualsVersions()
    {
        List<OSVersion> res=new ArrayList<>();
        for (OSVersion v : OSVersion.values())
            if (v.getOS()==this.getOS())
                if (v.ordinal()<=this.ordinal())
                    res.add(v);
        return res;
    }

    @SuppressWarnings("unused")
    public List<OSVersion> getGreaterVersions()
    {
        List<OSVersion> res=new ArrayList<>();
        for (OSVersion v : OSVersion.values())
            if (v.getOS()==this.getOS())
                if (v.ordinal()>this.ordinal())
                    res.add(v);
        return res;
    }

    @SuppressWarnings("unused")
    public List<OSVersion> getGreaterOrEqualVersions()
    {
        List<OSVersion> res=new ArrayList<>();
        for (OSVersion v : OSVersion.values())
            if (v.getOS()==this.getOS())
                if (v.ordinal()>=this.ordinal())
                    res.add(v);
        return res;
    }
    
    public static void main(String[] args)
    {
        System.out.println(OS_VERSION);
    	System.out.println(getCurrentOSVersion());
    }
}
