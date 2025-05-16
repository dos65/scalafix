package scalafix.interfaces.imports;

public interface Select extends TermRef {
  TermRef qualifier();
  String name();
}
