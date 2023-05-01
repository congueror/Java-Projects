import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SubtitleFixer {
    private static final Pattern time = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9],[0-9][0-9][0-9] --> [0-9][0-9]:[0-9][0-9]:[0-9][0-9],[0-9][0-9][0-9]");

    public static void main(String[] args) {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss,SSS");

        Scanner in = new Scanner(System.in);
        System.out.print("File name: ");
        String fileName = in.nextLine();
        File file = new File("./" + fileName);
        Path path = file.toPath();

        System.out.print("Rebase time?[Y/N]: ");
        String rebaseTime = in.nextLine();
        boolean rebase = rebaseTime.equals("Y") || (!rebaseTime.equals("N"));
        String start = "";
        if (rebase) {
            System.out.print("Subtitle rebase time[hh:mm:ss,SSS]: ");
            start = in.nextLine();
        }

        System.out.print("Delete entries?[Y/N]: ");
        String deleteEntries = in.nextLine();
        boolean delete = deleteEntries.equals("Y") || (!deleteEntries.equals("N"));
        int delNum = -1;
        if (delete) {
            System.out.print("Delete up to entry(exclusive): ");
            delNum = Integer.parseInt(in.nextLine());
        }

        try {
            Date first = rebase ? f.parse(start) : null;
            List<String> lines = Files.readAllLines(path);

            if (delete) {
                removeLines(lines, delNum);
            }


            long difference = 0;
            int currentIndex = 0;
            for (int i = 0; i < lines.size(); i++) {
                String s = lines.get(i);

                if (time.matcher(s).matches()) {
                    if (delete) {
                        currentIndex++;
                        lines.set(i - 1, String.valueOf(currentIndex));
                    }

                    if (rebase) {
                        Date t1 = f.parse(s.substring(0, 12));
                        Date t2 = f.parse(s.substring(17));

                        if (difference == 0)
                            difference = t1.getTime() - first.getTime();

                        String news = "";
                        news += f.format(Date.from(t1.toInstant().minus(difference, ChronoUnit.MILLIS)));
                        news += " --> ";
                        news += f.format(Date.from(t2.toInstant().minus(difference, ChronoUnit.MILLIS)));

                        lines.set(i, news);
                    }
                }
            }

            Files.write(path, lines);
        } catch (Exception e) {
        }
    }

    private static void removeLines(List<String> lines, int index) {
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i);

            if (time.matcher(s).matches() && Integer.parseInt(lines.get(i - 1)) == index) {
                lines.subList(0, i - 1).clear();
                break;
            }
        }
    }
}