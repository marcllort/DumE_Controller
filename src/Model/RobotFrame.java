package Model;

public class RobotFrame {

    private int[] values;

    public RobotFrame(int ... frames){
        values = new int[2];

        for(int i = 0 ;i < 2; i++){
            int[] initFrameAngle = {90, 90};
            if(i < frames.length) values[i] = frames[i];
            else values[i] = initFrameAngle[i];
        }
    }

    public static RobotFrame copy(RobotFrame frame) {
        return new RobotFrame(frame.getValues());
    }

    public int[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "[ " + values[0] + " " + values[1] + " ]";
    }

    public String toJSON(boolean last) {
        String aux = "{";
        for(int i= 0; i < 2; i++){
            aux += "\"" + i + "\":" + "\"" + values[i];
            if(i == 0)aux+= "\",";
            else aux+= "\"";
        }
        aux += "}";
        if(!last) aux += ",";
        return aux;
    }

    public void updateValue(int newVal, int index){
        values[index] = newVal;
    }
}