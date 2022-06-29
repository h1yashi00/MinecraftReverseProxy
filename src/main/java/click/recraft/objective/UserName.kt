package click.recraft.objective

class UserName(_name: String) {
    val name: String
    init {
        check(3 <= _name.length)
        check(_name.length <= 17)
        name = _name
    }
}