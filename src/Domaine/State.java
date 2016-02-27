package Domaine;

public enum State {
	Closed,
	AuthorizationUser,
	AuthorizationPassword,
	Transaction,
	Update,
	TimeOut,
	WaitForWelcome,
	WaitForLogin,
	WaitForPassword,
	WaitForTransaction,
	ErrorCommand
}
