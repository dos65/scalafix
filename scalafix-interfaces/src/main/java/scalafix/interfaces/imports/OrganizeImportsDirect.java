package scalafix.interfaces.imports;

import java.util.List;

public interface OrganizeImportsDirect {

  List<Import> organize(List<Import> in);
}