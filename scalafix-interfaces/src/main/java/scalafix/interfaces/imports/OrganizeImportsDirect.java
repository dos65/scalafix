package scalafix.interfaces.imports;

import java.util.List;

public interface OrganizeImportsDirect {

  List<List<Import>> organize(List<Import> in);

  public static OrganizeImportsDirect noopInstance() {
    OrganizeImportsDirect dummy =
      new OrganizeImportsDirect() {
        public List<List<Import>> organize(List<Import> in) {
          return List.of(in);
        }
      };
    return dummy;
  }
}