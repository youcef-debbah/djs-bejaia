package dz.ngnex.bean;

import dz.ngnex.entity.AccountDemandEntity;

import javax.ejb.Local;
import java.util.List;

@Local
public interface AccountDemandBean {
  AccountDemandEntity send(AccountDemandEntity demand);

  List<AccountDemandEntity> getDemands();

  void delete(AccountDemandEntity entity);

  AccountDemandEntity find(Integer demandID);

  void clear();
}
