package encryptdecrypt;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

enum Mode {
    ENCRYPT, DECRYPT;
}

enum Algorithm {
    UNICODE, SHIFT
}

enum IO {
    CONSOLE, FILE, UNSET;
}

abstract class EncryptionAlgorithm {
    protected char[] msg;
    protected final int key;

    EncryptionAlgorithm(String text, int key) {
        this.msg = text.toCharArray();
        this.key = key;
    }

    public abstract void encrypt();

    public abstract void decrypt();

    public String getResult() {
        return new String(msg);
    }
}

class UnicodeAlgorithm extends EncryptionAlgorithm {
    UnicodeAlgorithm(String text, int key) {
        super(text, key);
    }

    public void encrypt() {
        for (int i = 0; i < msg.length; i++) {
            msg[i] = (char) (msg[i] + key);
        }
    }

    public void decrypt() {
        for (int i = 0; i < msg.length; i++) {
            msg[i] = (char) (msg[i] - key);
        }
    }
}

class ShiftAlgorithm extends EncryptionAlgorithm {
    ShiftAlgorithm(String text, int key) {
        super(text, key);
    }

    public void encrypt() {
        for (int i = 0; i < msg.length; i++) {
            if (msg[i] >= 'a' && msg[i] <= 'z') {
                if (msg[i] + key <= 'z') {
                    msg[i] = (char) (msg[i] + key);
                } else {
                    msg[i] = (char) ('a' + ((msg[i] + key) - 'z') - 1);
                }
            }
            if (msg[i] >= 'A' && msg[i] <= 'Z') {
                if (msg[i] + key <= 'Z') {
                    msg[i] = (char) (msg[i] + key);
                } else {
                    msg[i] = (char) ('A' + ((msg[i] + key) - 'Z') - 1);
                }
            }
        }
    }

    public void decrypt() {
        for (int i = 0; i < msg.length; i++) {
            if (msg[i] >= 'a' && msg[i] <= 'z') {
                if (msg[i] - key >= 'a') {
                    msg[i] = (char) (msg[i] - key);
                } else {
                    msg[i] = (char) ('z' + ((msg[i] - key) - 'a') + 1);
                }
            } else if (msg[i] >= 'A' && msg[i] <= 'Z') {
                if (msg[i] - key >= 'A') {
                    msg[i] = (char) (msg[i] - key);
                } else {
                    msg[i] = (char) ('Z' + ((msg[i] - key) - 'A') + 1);
                }
            }
        }
    }
}

public class Main {
    static int key = 0;
    static Mode mode = Mode.ENCRYPT;
    static String input = "";
    static String in = "";
    static String out = "";
    static IO inputMode = IO.UNSET;
    static IO outputMode = IO.CONSOLE;
    static Algorithm alg = Algorithm.SHIFT;

    private static void readFile() throws IOException {
        input = new String(Files.readAllBytes(Paths.get(in)));
    }

    private static void writeToFile(String s) throws IOException {
        FileWriter writer = new FileWriter(out);
        writer.write(s);
        writer.close();
    }

    public static void parseInput(String[] args) throws Exception {
        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-mode":
                    if (args[i + 1].equals("enc")) {
                        mode = Mode.ENCRYPT;
                    } else if (args[i + 1].equals("dec")) {
                        mode = Mode.DECRYPT;
                    } else {
                        throw new Exception(String.format("Error: Unknown mode '%s', options are 'enc' and 'dec'", args[i + 1]));
                    }
                    break;
                case "-key":
                    key = Integer.parseInt(args[i + 1]);
                    break;
                case "-data":
                    input = args[i + 1];
                    inputMode = IO.CONSOLE;
                    break;
                case "-in":
                    in = args[i + 1];
                    if (inputMode == IO.UNSET) {
                        inputMode = IO.FILE;
                        readFile();
                    }
                    break;
                case "-out":
                    out = args[i + 1];
                    outputMode = IO.FILE;
                    break;
                case "-alg":
                    if (args[i + 1].equals("unicode")) {
                        alg = Algorithm.UNICODE;
                    } else if (args[i + 1].equals("shift")) {
                        alg = Algorithm.SHIFT;
                    } else {
                        throw new Exception(String.format("Unknown algorithm '%s', options are 'shift' and 'unicode'", args[i + 1]));
                    }
                    break;
                default:
                    throw new Exception(String.format("Error: Unknown argument %s", args[i]));
            }
        }
    }

    public static void main(String[] args) {
        try {
            parseInput(args);
            EncryptionAlgorithm encryptionAlgorithm;
            if (alg == Algorithm.UNICODE) {
                encryptionAlgorithm = new UnicodeAlgorithm(input, key);
            } else {
                encryptionAlgorithm = new ShiftAlgorithm(input, key);
            }
            if (mode == Mode.ENCRYPT) {
                encryptionAlgorithm.encrypt();
            } else {
                encryptionAlgorithm.decrypt();
            }
            if (outputMode == IO.CONSOLE) {
                System.out.println(encryptionAlgorithm.getResult());
            } else {
                writeToFile(encryptionAlgorithm.getResult());
            }
        } catch (IOException e) {
            System.out.printf("Error - file: %s%n", e.getMessage());
        } catch (Exception e) {
            System.out.printf("Error: %s%n", e.getMessage());
        }
    }
}