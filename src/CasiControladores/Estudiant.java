package CasiControladores;

import com.fazecast.jSerialComm.SerialPort;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static java.lang.Thread.sleep;

/***
 * Classe a modificar per l'estudiant de SDM.
 *
 * Nota: S'ha de vincular la carpeta libs al projecte!
 *       JDK 8.0 OBLIGATORI!
 */

public class Estudiant {

    //S'inicialitza automàticament quan es prem el botó -Connect- de la finestra Serial Settings
    public static SerialPort mySerial;

    //Període en ms amb el que es crida la funció readMode().
    public static final int READ_PERIOD = 1000;

    //Període en ms amb el que es crida la funció writeMode() quan l'Animator té activat Writing i es modifica l'angle
    //d'un motor. Serveix per a no cridar wirteMode() infinites vegades al modificar l'angle dels motors.
    public static final long WRITE_PERIOD = 1000;

    //Aquesta funció es crida cada cop que es modifica la posició dels motors en l'Animator i està activada
    //l'opció Writting.
    //Rep els dos angles dels motors com arguments.
    public static void writeMode(int angleMotor1, int angleMotor2){
        System.out.println("Aquí va el codi que escriu en el port sèrie.\nEnviant angles " + angleMotor1 + " " + angleMotor2);

        //byte[] writteArray = intToByte(angleMotor1,angleMotor2);

        //ByteBuffer b = ByteBuffer.allocate(4);
        //b.putInt(angleMotor1);
        //b.putInt(199);
        //byte[] result = {(byte) angleMotor1, (byte) angleMotor2};
        //byte[] result = b.array();
        //byte[] enviabyte = new byte[2];
        //enviabyte[0]=intToByte();
        //enviabyte[1]=0;


        Float value0, value1;
        value0= Float.valueOf(angleMotor1);
        value1= Float.valueOf(angleMotor2);
        value0=(value0/180)*255;
        value1=(value1/180)*255;

        angleMotor1=value0.intValue();
        angleMotor2=value1.intValue();

        byte[] result = {(byte) angleMotor1, (byte) angleMotor2};

        String s1 = String.format("%8s", Integer.toBinaryString(result[0] & 0xFF)).replace(' ', '0');
        String s2 = String.format("%8s", Integer.toBinaryString(result[1] & 0xFF)).replace(' ', '0');
        System.out.println("Motor 0: "+s1+"   Motor 2: "+s2);



        mySerial.writeBytes(result,result.length);


    }

    //Aquesta funció es crida periòdicament (cada READ_PERIOD) quan el mode Reading està activat en l'Animator (sempre) i
    //serveix per a modificar l'angle dels motors del frame actual.
    //La funció ha de retornar un array de 2 ints omplert amb els nous angles dels motors.
    //El primer int correspon a l'angle del primer motor.
    //El segon int correspon a l'angle del segon motor.
    public static int[] readMode(){
        int length = mySerial.bytesAvailable();

        byte[] readAarr = new byte[length];
        int byteRead = mySerial.readBytes(readAarr,length);

        if (byteRead != length){
            System.out.println("No s'ha llegit correctament");
        }

        int [] exemple = new int [2];
        exemple[0] = readAarr[0];
        exemple[1] = readAarr[1];

        if(exemple[0]<0){
            exemple[0]=127+exemple[0];
            exemple[0]=exemple[0]+127;
        }
        if(exemple[1]<0){
            exemple[1]=127+exemple[1];
            exemple[1]=exemple[1]+127;
        }
        Float value0, value1;
        value0= Float.valueOf(exemple[0]);
        value1= Float.valueOf(exemple[1]);
        value0=(value0/255)*180;
        value1=(value1/255)*180;
        exemple[0]=value0.intValue();
        exemple[1]=value1.intValue();

        //System.out.println("S'acaba d'executar una actualització dels motors de l'Animator amb els següents valors:");
        //System.out.println("Motor1 = " + exemple[0]);
        //System.out.println("Motor2 = " + exemple[1]);


        return exemple;
    }

    //Funció creada per convertir els dos integers a un array de bytes per poder-los enviar
    private static byte[] intToByte(int angleMotor1, int angleMotor2){
        /*Float value0, value1;
        value0= Float.valueOf(angleMotor1);
        value1= Float.valueOf(angleMotor2);
        value0=(value0/180)*255;
        value1=(value1/180)*255;

        angleMotor1=value0.intValue();
        angleMotor2=value1.intValue();*/



        BigInteger i = BigInteger.valueOf(angleMotor1);
        byte []  array = i.toByteArray();
        i = BigInteger.valueOf(angleMotor2);
        byte []  array2 = i.toByteArray();


        byte[] combined = new byte[array.length + array2.length];

        for (int j = 0; j < combined.length; ++j)
        {
            combined[j] = j < array.length ? array[j] : array2[j - array2.length];
        }
        return combined;
    }



//Com se de cuants bytes es la info que m'arriba al llegir
    public static long byteToInt(byte[] bytes, int length) {
        int val = 0;
        if(length>4) throw new RuntimeException("Too big to fit in int");
        for (int i = 0; i < length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }


}
