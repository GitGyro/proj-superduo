package barqsoft.footballscores;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.Time;

import java.text.SimpleDateFormat;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{
    /*
     * used by app widget to filter list and update match day being displayed
     */
    public static final Integer MAX_DAYS_LOOKAHEAD = 3;
    public static final Integer BASE_TODAY_ID = 3;
    public static final Integer MAX_DAY_INDEX = 6;

    public static Integer widgetDaySelection =BASE_TODAY_ID;

    public static final int CHAMPIONS_LEAGUE = 362;

    public static final int BUNDESLIGA1 = 394;
    public static final int BUNDESLIGA2 = 395;
    public static final int LIGUE1 = 396;
    public static final int LIGUE2 = 397;
    public static final int PREMIER_LEGAUE = 398;
    public static final int PRIMERA_DIVISION = 399;
    public static final int SERIE_A = 401;
    public static final int PRIMERA_LIGA=402;
    public static final int BUNDESLIGA3 = 403;
    public static final int EREDIVISIE = 404;

    public static String [] leagueName;
    public static String [] matchDay;
    public static String [] teamNames;

    public static String getLeague(Context context, int league_num)
    {
        if (leagueName == null)  {
            Resources res = context.getResources();
            leagueName = res.getStringArray(R.array.leagueName);
        }
        switch (league_num)
        {

            case CHAMPIONS_LEAGUE : return leagueName[0]; //"UEFA Champions League";

            case BUNDESLIGA1 :      return leagueName[1]; //"Bundesliga 1";
            case BUNDESLIGA2 :      return leagueName[2]; //"Bundesliga 2";
            case LIGUE1 :           return leagueName[3]; //"LIGUE 1";
            case LIGUE2 :           return leagueName[4]; //"LIGUE 2";
            case PREMIER_LEGAUE :   return leagueName[5]; //"Premier League";
            case PRIMERA_DIVISION : return leagueName[6]; //"Primera Division";
            case SERIE_A :          return leagueName[7]; //"Seria A";
            case PRIMERA_LIGA :     return leagueName[8]; //"PRIMERA_LIGA";
            case BUNDESLIGA3 :      return leagueName[9]; //"Bundesliga 3";
            case EREDIVISIE :       return leagueName[10]; //"EREDIVISIE";
            default:                return leagueName[11]; //"Not known League Please report";
        }
    }
    /*
     * Moved strings to strings.xml
     */
    public static String getMatchDay(Context context, int match_day,int league_num)
    {
        if (leagueName == null) {
            Resources res = context.getResources();
            matchDay = res.getStringArray(R.array.matchDay);
        }
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return  matchDay[0]; //"Group Stages, Matchday : 6";
            }
            else if(match_day == 7 || match_day == 8)
            {
                return matchDay[1]; //"First Knockout round";
            }
            else if(match_day == 9 || match_day == 10)
            {
                return matchDay[2]; //"QuarterFinal";
            }
            else if(match_day == 11 || match_day == 12)
            {
                return matchDay[3]; //"SemiFinal";
            }
            else
            {
                return matchDay[4]; //"Final";
            }
        }
        else
        {
            return matchDay[5]+ String.valueOf(match_day); //"Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (Context context, String teamname)
    {
        if (teamname==null){return R.drawable.no_icon;}
        if (teamNames == null)  {
            Resources res = context.getResources();
            teamNames = res.getStringArray(R.array.teamName);
        }
        if (teamname.equals(teamNames[0])) return R.drawable.arsenal;
        else if (teamname.equals(teamNames[1])) return R.drawable.aston_villa;
        else if (teamname.equals(teamNames[2])) return R.drawable.burney_fc_hd_logo;
        else if (teamname.equals(teamNames[3])) return R.drawable.chelsea;
        else if (teamname.equals(teamNames[4])) return R.drawable.crystal_palace_fc;
        else if (teamname.equals(teamNames[5])) return R.drawable.everton_fc_logo1;
        else if (teamname.equals(teamNames[6])) return R.drawable.hull_city_afc_hd_logo;
        else if (teamname.equals(teamNames[7])) return R.drawable.leicester_city_fc_hd_logo;
        else if (teamname.equals(teamNames[8])) return R.drawable.liverpool;
        else if (teamname.equals(teamNames[9])) return R.drawable.manchester_city;
        else if (teamname.equals(teamNames[10])) return R.drawable.manchester_united;
        else if (teamname.equals(teamNames[11])) return R.drawable.newcastle_united;
        else if (teamname.equals(teamNames[12])) return R.drawable.queens_park_rangers_hd_logo;
        else if (teamname.equals(teamNames[13])) return R.drawable.southampton_fc;
        else if (teamname.equals(teamNames[14])) return R.drawable.stoke_city;
        else if (teamname.equals(teamNames[15])) return R.drawable.sunderland;
        else if (teamname.equals(teamNames[16])) return R.drawable.swansea_city_afc;
        else if (teamname.equals(teamNames[17])) return R.drawable.tottenham_hotspur;
        else if (teamname.equals(teamNames[18])) return R.drawable.west_bromwich_albion_hd_logo;
        else if (teamname.equals(teamNames[19])) return R.drawable.west_ham;
        else return R.drawable.no_icon;
        /*
        switch (teamname)
        { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Aston Villa" : return R.drawable.aston_villa;
            case "Burney FC" : return R.drawable.burney_fc_hd_logo;
            case "Chelsea" : return R.drawable.chelsea;
            case "Crystal Palace FC" : return R.drawable.crystal_palace_fc;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "Hull City AFC" : return R.drawable.hull_city_afc_hd_logo;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Liverpool" : return R.drawable.liverpool;
            case "Manchester City" : return R.drawable.manchester_city;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Newcastle United FC" : return R.drawable.newcastle_united;
            case "Queens Parks Rangers" : return R.drawable.queens_park_rangers_hd_logo;
            case "Southampton FC" : return R.drawable.southampton_fc;
            case "Stoke City FC" : return R.drawable.stoke_city;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "West Ham United FC" : return R.drawable.west_ham;
            default: return R.drawable.no_icon;
        }
        */
    }

    public static int getWidgetDaySelection(){
        return widgetDaySelection;
    }

    /**
     *
     * @param position range -2 .. 2 -1 = yesterday, 0 == today, 1 = tomorrow etc
     * @return current day selection i app widget
     */

    public static int setWidgetDaySelection(int position){
        Integer currentSelection = widgetDaySelection;
        widgetDaySelection = position;
        return widgetDaySelection;
    }
    /**
     *
     * @return custom day name string
     */
    public static String getDayName(Context context) {

        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        long dateInMillis = System.currentTimeMillis()+((widgetDaySelection-BASE_TODAY_ID)*86400000);
        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return   context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        }
        else if ( julianDay == currentJulianDay -1)
        {
            return context.getString(R.string.yesterday);
        }
        else
        {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }


}
