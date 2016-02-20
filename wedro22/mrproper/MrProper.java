package wedro22.mrproper;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.nio.file.StandardOpenOption;

/**
 * Класс для работы с файлами настроек в формате Map [String, Object], имеет
 * свой Map.
 * <p>
 * Поддерживаемые типы Object: Integer, Double, String, Boolean. 
 * Иные типы конвертируются toString.
 * <p>
 * Файл настроек, например Test.properties имеет вид:
 * <pre>
 *     S:str=qwerty
 *     B:boo=true
 *     D:Pi=3.14159
 *     I:i=42
 * </pre>
 * , где
 * <pre>
 *     Тип:Ключ=Значение
 * </pre>
 * При неверной форме записи строка игнорируется.
 * Пример использования класса:
 * <pre>
 *      MrProper mrProp = new MrProper();
 *      
 *      //Настройки по умолчанию
 *      mrProp.put("name", "test");
 *      mrProp.put("Pi", (Double)3.14);
 *      mrProp.put("boo", (Boolean)false);
 *      
 *      //Загрузка настроек из файла (при совпадении ключей в Test.properties
 *      //и mrProp, заменит в нем настройки
 *      mrProp.compareMap(mrProp.getLoadMap("Test.properties"));
 *      
 *      //Вывод содержимого класса MrProper
 *      System.out.println(mrProp.toStringAdvanced());
 *      // Выведет:     Boolean boo = true
 *      //              String name = test
 *      //              Double Pi = 3.14159
 *      
 *      //Сохранение настроек в файл.
 *      mrProp.save("Test2.properties", null);
 *      // Сохранит:    B:boo=true
 *      //              S:name=test
 *      //              D:Pi=3.14159
 * </pre>
 * @author wedro22
 * @author копипаста
 * @version 2.0
 * @since 1.7
 */
public class MrProper{
    private Map<String, Object> map=new HashMap<>();
    
    
//<editor-fold defaultstate="collapsed" desc="put/get">
    public void put(String name,int i){
        map.put(name, (Integer)i);
    }
    public void put(String name,boolean b){
        map.put(name, (Boolean)b);
    }
    public void put(String name, double d){
        map.put(name, (Double)d);
    }
    public void put(String name, Object o){
        map.put(name, o);
    }
    
    public <T> T get(String name){
        try {
            return ( T) map.get(name);
        } catch (ClassCastException ex) {
            System.out.println(ex);
            return null;
        }
    }

    /**
     * @return int количество пар Ключ = Объект 
     */
    public int getSize(){
        return map.size();
    }
//</editor-fold>
    

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     *  toString вида: ClassName Key = Value\r\n
     * @return null, если пустой
     */
    public StringBuilder toStringAdvanced() {
        final int mz=map.size();
        if (mz>0){
            StringBuilder sb=new StringBuilder(mz<<4);
            for (Entry<String, Object> en : map.entrySet()){
                sb.append(en.getValue().getClass().getSimpleName());
                sb.append(" ");
                sb.append(en.getKey());
                sb.append(" = ");
                sb.append(en.getValue());
                sb.append(System.lineSeparator());
            }
            sb.trimToSize();
            return sb;
        }
        return null;
    }
    
    /**
     *  Очистка Map класса
     */
    public void clear(){
        map.clear();
    }
    
    /**
     *  Загружает properties из файла в Map [String, Object] класса
     * @param s Полный путь к файлу
     * @return false, если ошибки (файл не найден или нечитаем)
     */
    public boolean load(String s){
        Path path=Paths.get(s);
        if (!Files.exists(path)){
            System.err.println("File not found: %s");
            return false;
        }
        if (!Files.isReadable(path)){
            System.err.println("File can not read: %s");
            return false;
        }
        
        try {
            List<String> list = Files.readAllLines(path, 
                    StandardCharsets.UTF_8);
            for (String en : list)
                getLine(en, map);
        } catch (IOException ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }
    
    /**
     *  Возвращает загруженный properties из файла в формате 
     *  Map [String, Object]
     * @param s Полный путь к файлу
     * @return null, если ошибки (файл не найден или нечитаем)
     */
    public Map<String, Object> getLoadMap(String s){
        Path path=Paths.get(s);
        if (!Files.exists(path)){
            System.err.println("File not found: %s");
            return null;
        }
        if (!Files.isReadable(path)){
            System.err.println("File can not read: %s");
            return null;
        }
        
        Map<String, Object> retMap;
        try {
            List<String> list = Files.readAllLines(path, 
                    StandardCharsets.UTF_8);
            retMap=new HashMap(list.size());
            for (String en : list)
                getLine(en, retMap);
             
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }
        return retMap;
    }
    private void getLine(String s, Map<String, Object> mp){
        if (s.length()<5) return;
        char c = s.charAt(0);
        //(String).matches
        if (s.charAt(1)!=':'||(c!='I'&&c!='B'&&c!='S'&&c!='D')) return;
        s=s.substring(2);
        String[]str=s.split("=", 2);
        if (!str[0].matches("(?i).*[0-9a-z].*")||
                !str[1].matches("(?i).*[0-9a-z].*"))
            return;
        
        try {
            switch (c) {
                case 'I': mp.put(str[0], Integer.valueOf(str[1])); break;
                case 'B': mp.put(str[0], Boolean.valueOf(str[1])); break;
                case 'D': mp.put(str[0], Double.valueOf(str[1]));  break;
                default:  mp.put(str[0], str[1]);                  break;
            }
        } catch (NumberFormatException nfe) {
            System.err.println("File properties error in line: %s"
                    +nfe.getLocalizedMessage());
        }
    }
    
    /**
     *  Сохраняет текущий Map в файл
     * @param s полный путь к файлу
     * @param mp Сохраняемый Map [String, Object]. Если null, то сохраняется
     * Map класса
     * @return false, если ошибки(категория вместо файла, файл нельзя записать)
     */
    public boolean save(String s, Map<String, Object> mp){
        Path path=Paths.get(s);
        if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)){
            System.err.println("File creating: It's directory, not file: %s");
            return false;
        }
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException ex) {
            System.err.println(ex);
            return false;
        }
        if (!Files.isWritable(path)){
            System.err.println("File can not write: %s");
            return false;
        }
        if (mp==null) mp=map;
        
        StringBuilder sb = new StringBuilder();
        for(Entry<String, Object> en : mp.entrySet()) {
            if (en.getValue().getClass() == Integer.class)
                sb.append("I:");
            else if (en.getValue().getClass() == Boolean.class)
                sb.append("B:");
            else if (en.getValue().getClass() == Double.class)
                sb.append("D:");
            else
                sb.append("S:");
            
            sb.append(en.getKey());
            sb.append("=");
            sb.append(en.getValue().toString());
            sb.append(System.lineSeparator());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardCharsets.UTF_8, StandardOpenOption.WRITE)) {
            writer.write(sb.toString());
        } catch (Exception e) { System.err.println(e); }
        return true;
    }

    /**
     *  ForEach. Пример кода:
     * <pre>
     *  for (Entry en : mrProp.entrySet()){
     *      System.out.println(en.getKey())
     *  }
     * </pre>
     * @return Map.entrySet() класса
     */
    public Iterable<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
    
    /**
     * Возвращает Map [String, Object], внося изменения в mDefolt из mChange
     * при наличии ключа в обоих Map
     * @param mDefolt значения по умолчанию
     * @param mChange новые значения
     * @return Map [String, Object], не изменяя Map класса
     */
    public Map<String, Object> getCompareMap(Map<String, Object> mDefolt, 
            Map<String, Object> mChange){
        Map map0=mDefolt;
        for(Entry<String, Object> en : mDefolt.entrySet()) {
            if (mChange.containsKey(en.getKey())){
                map0.put(en.getKey(), mChange.get(en.getKey()));
            }
        }
        return map0;
    }
    
    /**
     * Изменяет Map класса
     * @param mChange изменяемые значения в Map класса при наличии ключа
     * в обоих Map
     */
    public void compareMap(Map<String, Object> mChange){
        Map map0=map;
        for(Entry<String, Object> en : map.entrySet()) {
            if (mChange.containsKey(en.getKey())){
                map0.put(en.getKey(), mChange.get(en.getKey()));
            }
        }
        map=map0;
    }
    
}
