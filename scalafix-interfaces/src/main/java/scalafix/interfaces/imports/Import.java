package scalafix.interfaces.imports;

import java.util.List;

public interface Import {
  List<Importer> importers();
}
