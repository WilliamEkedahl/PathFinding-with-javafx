// PROG2 VT24, InlÃ¤mningsuppgift, del 2
// Grupp 159
// Max Lindberg mali7984
// William Ekedahl wiek0904
// Simon Lundqvist silu8199


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