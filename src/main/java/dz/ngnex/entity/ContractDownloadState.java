package dz.ngnex.entity;

public enum ContractDownloadState {
  NO_CONTRACT_ASSIGNED_YET, // downloads: 0 and total: 0
  NO_ASSIGNED_CONTRACT_DOWNLOADED, // downloads: 0 and total: 1
  ASSIGNED_CONTRACTS_PARTIALLY_DOWNLOADED, // downloads: 1 and total: 2
  ALL_ASSIGNED_CONTRACTS_DOWNLOADED, // downloads: 2 and total: 2
}
