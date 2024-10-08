public class Valid {
    static boolean isValidInt(String time){
        try{
            Integer.parseInt(time);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
