import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Genera una melodía infantil ORIGINAL de 30 segundos.
 * No usa muestras ni melodías externas: todos los instrumentos se sintetizan aquí.
 */
public final class GenerateMenuMusic {
    private static final int SR = 44100;
    private static final double DURACION = 30.0;
    private static final int N = (int) (SR * DURACION);
    private static final double[] L = new double[N];
    private static final double[] R = new double[N];
    private static final Random RND = new Random(20260730L);
    private static final double CORCHEA = 60.0 / 105.0 / 2.0;

    private static double hz(int midi) {
        return 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
    }

    private static void sumar(double inicio, double duracion, int midi, double volumen,
                              double panorama, int instrumento) {
        int desde = (int) (inicio * SR);
        int muestras = (int) (duracion * SR);
        double f = hz(midi);
        for (int j = 0; j < muestras && desde + j < N; j++) {
            double t = j / (double) SR;
            double ataque = Math.min(1.0, t / .012);
            double s;
            if (instrumento == 0) { // xilófono cálido
                double env = ataque * Math.exp(-4.8 * t);
                s = env * (Math.sin(2*Math.PI*f*t)
                    + .34*Math.sin(2*Math.PI*f*3.01*t)
                    + .16*Math.sin(2*Math.PI*f*5.02*t));
            } else if (instrumento == 1) { // campanita
                double env = ataque * Math.exp(-2.6 * t);
                s = env * (Math.sin(2*Math.PI*f*t)
                    + .42*Math.sin(2*Math.PI*f*2.01*t)
                    + .22*Math.sin(2*Math.PI*f*4.08*t));
            } else if (instrumento == 2) { // piano brillante
                double env = ataque * Math.exp(-3.0 * t);
                s = env * (Math.sin(2*Math.PI*f*t)
                    + .28*Math.sin(2*Math.PI*f*2*t)
                    + .10*Math.sin(2*Math.PI*f*3*t));
            } else { // metal juguetón, redondeado
                double env = Math.min(1.0, t/.035) * Math.exp(-2.2*t);
                s = env * (Math.sin(2*Math.PI*f*t)
                    + .20*Math.sin(2*Math.PI*f*2*t)
                    + .08*Math.sin(2*Math.PI*f*3*t));
            }
            int i = desde + j;
            L[i] += s * volumen * Math.sqrt((1.0 - panorama) * .5);
            R[i] += s * volumen * Math.sqrt((1.0 + panorama) * .5);
        }
    }

    private static void percusion(double inicio, boolean palma) {
        int desde = (int)(inicio * SR), muestras = (int)((palma ? .11 : .16) * SR);
        double ultimo = 0;
        for (int j=0; j<muestras && desde+j<N; j++) {
            double t=j/(double)SR, ruido=RND.nextDouble()*2-1;
            double filtrado=ruido-ultimo*.72; ultimo=ruido;
            double env=Math.exp(-(palma ? 28 : 20)*t);
            double tono=palma ? 0 : Math.sin(2*Math.PI*(150-55*t/.16)*t)*.8;
            double s=(palma ? filtrado : tono+ruido*.12)*env*(palma?.055:.075);
            L[desde+j]+=s; R[desde+j]+=s;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Ruta WAV requerida");

        // Cinco compases cortos de 3,5 pulsos = frase de 10 s; se repite tres veces.
        int[][] melodia = {
            {72,76,79,76,74,72,67},
            {74,77,81,77,76,74,69},
            {76,79,83,79,77,76,71},
            {74,77,81,79,77,74,69},
            {72,76,79,84,79,76,72}
        };
        int[][] acordes = {{60,64,67},{62,65,69},{64,67,71},{65,69,72},{60,64,67}};

        for (int vuelta=0; vuelta<3; vuelta++) {
            for (int celda=0; celda<5; celda++) {
                double base=vuelta*10.0+celda*2.0;
                for (int k=0;k<7;k++) {
                    sumar(base+k*CORCHEA, .42, melodia[celda][k],
                        vuelta==1 ? .105 : .115, -.18+(k%3)*.18, 0);
                    if ((k==0 || k==4) && vuelta>0)
                        sumar(base+k*CORCHEA, .65, melodia[celda][k]+12, .038, .35, 1);
                }
                for (int nota:acordes[celda])
                    sumar(base, 1.75, nota, .037, (nota%3-1)*.25, 2);
                sumar(base+3*CORCHEA, .42, melodia[celda][3]-12, .045, .20, 3);
                for (int k=0;k<7;k++) {
                    percusion(base+k*CORCHEA, false);
                    if (k==2 || k==6) percusion(base+k*CORCHEA, true);
                }
            }
        }

        // Eco corto y limitador suave; la señal termina limpia justo en 30 s.
        int eco=(int)(.145*SR);
        for (int i=eco;i<N;i++) {
            L[i]+=L[i-eco]*.10; R[i]+=R[i-eco]*.10;
        }
        ByteBuffer pcm=ByteBuffer.allocate(N*4).order(ByteOrder.LITTLE_ENDIAN);
        for (int i=0;i<N;i++) {
            double borde=Math.min(1, Math.min(i/(SR*.025), (N-1-i)/(SR*.025)));
            pcm.putShort((short)(Math.tanh(L[i]*1.25)*borde*24500));
            pcm.putShort((short)(Math.tanh(R[i]*1.25)*borde*24500));
        }
        try (DataOutputStream out=new DataOutputStream(new FileOutputStream(args[0]))) {
            int bytes=pcm.capacity();
            out.writeBytes("RIFF"); writeLE(out,36+bytes); out.writeBytes("WAVEfmt ");
            writeLE(out,16); writeLES(out,1); writeLES(out,2); writeLE(out,SR);
            writeLE(out,SR*4); writeLES(out,4); writeLES(out,16);
            out.writeBytes("data"); writeLE(out,bytes); out.write(pcm.array());
        }
    }
    private static void writeLE(DataOutputStream o,int v)throws IOException {
        o.writeByte(v);o.writeByte(v>>8);o.writeByte(v>>16);o.writeByte(v>>24);
    }
    private static void writeLES(DataOutputStream o,int v)throws IOException {
        o.writeByte(v);o.writeByte(v>>8);
    }
}
