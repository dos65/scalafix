package scalafix.interfaces.imports;

import java.util.List;

public interface Importer {
  TermRef ref();
  List<String> importees();
}