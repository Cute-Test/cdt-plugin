#define macro(X) A::f(X)
namespace A {
struct f {
	f(int) {
	}
};
void doit(f) {
}
}
int main() {
	A::f(0);
	doit(macro(42));
}
